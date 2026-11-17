# Preuve vérifiable côté Python : on compile en .pyc et on cherche le secret.
# Lancer : python3 test_secret_leak.py
import os
import py_compile
import subprocess
import sys
import tempfile
from pathlib import Path

# Construit à l'exécution : un littéral (ou "a" + "b", que Python plie à la
# compilation) mettrait le secret entier dans le .pyc du test lui-même.
SECRET = "".join(["SuperSecret_", "Prod_2026!"]).encode()
HERE = Path(__file__).parent


def pyc_contains_secret(source: str) -> bool:
    with tempfile.TemporaryDirectory() as tmp:
        out = Path(tmp) / (Path(source).stem + ".pyc")
        py_compile.compile(str(HERE / source), cfile=str(out), doraise=True)
        return SECRET in out.read_bytes()


def main() -> None:
    vuln = pyc_contains_secret("vulnerable_client.py")
    secure = pyc_contains_secret("secure_client.py")
    test_itself = pyc_contains_secret("test_secret_leak.py")
    print(f"vulnerable_client.pyc contient le secret : {vuln}")
    print(f"secure_client.pyc contient le secret     : {secure}")
    assert vuln, "La version vulnérable devrait fuiter"
    assert not secure, "La version corrigée ne doit pas fuiter"
    assert not test_itself, "Le test ne doit pas contenir le secret entier"

    # Sans variable d'env, le client corrigé refuse de démarrer.
    env = {k: v for k, v in os.environ.items() if k not in ("API_KEY", "API_URL")}
    r = subprocess.run([sys.executable, str(HERE / "secure_client.py")],
                       env=env, capture_output=True, text=True)
    assert r.returncode != 0 and "manquante" in r.stderr
    print("Sans variable d'env -> refus de démarrer")
    print("OK : tous les tests passent")


if __name__ == "__main__":
    main()
