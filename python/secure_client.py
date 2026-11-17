# CORRIGÉ : le secret vient de l'environnement (ou d'un coffre de secrets).
# Différences avec Java à montrer :
#  - pas de char[] effaçable : les str Python sont immuables, on ne peut pas
#    nettoyer la mémoire -> limite à mentionner
#  - piège classique : os.environ.get("API_KEY", "valeur_par_defaut") remet
#    un secret en dur via la valeur par défaut
#  - python-dotenv lit un fichier .env : OK en dev SI .env est dans .gitignore

import os


def require_env(name: str) -> str:
    value = os.environ.get(name)
    if not value:
        raise RuntimeError(f"Variable d'environnement manquante : {name}")
    return value


API_URL = require_env("API_URL")
API_KEY = require_env("API_KEY")


def auth_header() -> dict:
    return {"Authorization": f"Bearer {API_KEY}"}


if __name__ == "__main__":
    # On n'affiche jamais la clé elle-même.
    print(f"Appel de {API_URL} avec une clé de {len(API_KEY)} caractères")
