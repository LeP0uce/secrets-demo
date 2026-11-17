#!/usr/bin/env bash
# Démo complète pour la présentation "Secrets codés en dur".
# Prérequis : JDK 11+, Python 3, git. Lancer depuis le dossier secrets-demo :  bash demo.sh
# "strings" est fait par strings.py (portable) : le strings de macOS plante
# sur les .class (il les prend pour des binaires Mach-O). Sous Linux, la vraie
# commande `strings` donne le même résultat.
set -e
cd "$(dirname "$0")"
pause() { echo; read -rp "--- Entrée pour continuer ---"; echo; }

echo "=== 1. JAVA : le secret survit à la compilation ==="
cd java
javac -encoding UTF-8 *.java
echo "> javap -c -constants VulnerableDbClient | grep Secret"
javap -c -constants VulnerableDbClient | grep -m1 Secret
echo "> strings VulnerableDbClient.class | grep Secret"
python3 ../strings.py VulnerableDbClient.class Secret
pause

echo "=== 2. JAVA : version corrigée ==="
echo "> strings SecureDbClient.class | grep Secret   (rien attendu)"
python3 ../strings.py SecureDbClient.class Secret || echo "(aucun résultat : rien dans le binaire)"
echo "> lancement avec le secret injecté à l'exécution"
DB_URL="jdbc:postgresql://prod-db:5432/app" DB_USER="app_admin" DB_PASSWORD="vient_du_coffre" \
  java SecureDbClient
echo "> tests"
java SecretLeakTest
cd ..
pause

echo "=== 3. PYTHON : même faille, même correction ==="
cd python
python3 -m py_compile vulnerable_client.py
echo "> strings sur le .pyc"
python3 ../strings.py __pycache__/vulnerable_client.*.pyc Secret
python3 test_secret_leak.py
cd ..
pause

echo "=== 4. GIT : supprimer ne suffit pas, il faut révoquer ==="
TMP=$(mktemp -d)
git -C "$TMP" init -q
git -C "$TMP" config user.email demo@example.com
git -C "$TMP" config user.name demo
git -C "$TMP" config commit.gpgsign false      # pas de signature GPG dans la démo
git -C "$TMP" config core.hooksPath /dev/null   # ignore d'éventuels hooks globaux
echo 'DB_PASSWORD = "SuperSecret_Prod_2026!"' > "$TMP/config.py"
git -C "$TMP" add . && git -C "$TMP" commit -qm "ajout config"
echo 'DB_PASSWORD = os.environ["DB_PASSWORD"]' > "$TMP/config.py"
git -C "$TMP" commit -qam "retrait du secret"
echo "> fichier actuel :"; cat "$TMP/config.py"
echo "> mais l'historique : git log -p | grep Secret"
git -C "$TMP" log -p | grep Secret
rm -rf "$TMP"
echo
echo "Conclusion : un secret poussé est compromis -> le révoquer et en générer un nouveau."
