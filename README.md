# Secrets codés en dur (CWE-798 / CERT MSC03-J)

Démo et tests pour le cours ALTN96 « Le Secure Coding en action » (EFREI, 2026-2027).

> Le secret `SuperSecret_Prod_2026!` utilisé ici est **factice**, volontairement mis en dur pour la démonstration.

## Contenu

| Fichier | Rôle |
|---|---|
| `java/VulnerableDbClient.java` | Mot de passe en constante `static final` : il se retrouve dans le `.class` |
| `java/SecureDbClient.java` | Secret lu à l’exécution (`System.getenv`), échec si absent, jamais affiché, `char[]` effacé |
| `java/SecretLeakTest.java` | Test sans JUnit : lit les octets des `.class`, vérifie la fuite et la correction, et le refus de démarrer sans secret |
| `python/vulnerable_client.py` | Même faille en Python : la clé finit dans le `.pyc` |
| `python/secure_client.py` | `require_env()`, sans valeur par défaut |
| `python/test_secret_leak.py` | Compile en `.pyc`, cherche le secret, vérifie le refus de démarrer |
| `strings.py` | Équivalent portable de `strings` (macOS et Windows) |
| `demo.sh` | Démo pas à pas : Java, Python, historique Git |

## Lancer

Prérequis : JDK 11+, Python 3, git.

```bash
bash demo.sh
```

Ou seulement les tests :

```bash
cd java && javac -encoding UTF-8 *.java && java SecretLeakTest
cd python && python3 test_secret_leak.py
```

## Références

- SEI CERT Oracle Coding Standard for Java, MSC03-J « Never hard code sensitive information »
- MITRE CWE-798 « Use of Hard-coded Credentials »
- OWASP Secrets Management Cheat Sheet ; OWASP Top 10 A07:2021 / A07:2025
