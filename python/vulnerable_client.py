# VULNÉRABLE : CWE-798
# Python n'est pas "plus caché" que Java : la chaîne finit telle quelle
# dans le .pyc (bytecode), et un .pyc se décompile ou se lit avec strings.

API_URL = "https://api.example.com/v1"
API_KEY = "SuperSecret_Prod_2026!"


def auth_header() -> dict:
    return {"Authorization": f"Bearer {API_KEY}"}


if __name__ == "__main__":
    print(f"Appel de {API_URL} avec une clé de {len(API_KEY)} caractères")
