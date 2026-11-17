# Équivalent portable de la commande Unix `strings` (marche aussi sous Windows).
# Affiche les suites d'au moins 4 caractères ASCII imprimables d'un fichier binaire.
# Usage : python3 strings.py FICHIER [MOTIF]
#   - sans MOTIF : affiche toutes les chaînes
#   - avec MOTIF : n'affiche que celles qui le contiennent (comme `strings f | grep MOTIF`)
#     et renvoie le code 1 si rien n'est trouvé (comme grep)
#
# Pourquoi ce script : sous macOS, `strings` prend un .class Java pour un binaire
# Mach-O "fat" (même nombre magique 0xCAFEBABE) et plante au lieu de scanner le
# fichier. Une preuve d'ABSENCE ne doit pas reposer sur un outil qui a échoué.
import re
import sys

MIN_LEN = 4


def main() -> int:
    if len(sys.argv) not in (2, 3):
        print(__doc__ or "usage: strings.py FICHIER [MOTIF]", file=sys.stderr)
        return 2
    data = open(sys.argv[1], "rb").read()
    pattern = sys.argv[2] if len(sys.argv) == 3 else None
    found = False
    for m in re.finditer(rb"[\x20-\x7e]{%d,}" % MIN_LEN, data):
        s = m.group().decode("ascii")
        if pattern is None or pattern in s:
            print(s)
            found = True
    return 0 if found or pattern is None else 1


if __name__ == "__main__":
    sys.exit(main())
