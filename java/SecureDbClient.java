import java.util.Arrays;

// CORRIGÉ : le secret n'est plus dans le code.
// Il est fourni à l'exécution (variable d'environnement ici ; en prod :
// coffre de secrets type Vault / AWS Secrets Manager, ou KeyStore Java).
// Avantages : rien dans le .class, rien dans Git, rotation sans recompiler.
public class SecureDbClient {

    private final String url;
    private final String user;
    // char[] : on peut l'effacer après usage, contrairement à une String (immuable).
    // LIMITE : System.getenv() renvoie déjà une String, qui reste en mémoire jusqu'au
    // passage du GC, et la variable reste dans l'environnement du processus.
    // L'effacement est donc "best-effort" ; pour l'appliquer vraiment, lire le secret
    // directement en char[] (KeyStore, Console.readPassword, coffre de secrets).
    private final char[] password;

    public SecureDbClient() {
        this.url = requireEnv("DB_URL");
        this.user = requireEnv("DB_USER");
        this.password = requireEnv("DB_PASSWORD").toCharArray();
    }

    // Échoue tout de suite si le secret manque : pas de valeur par défaut en dur.
    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Variable d'environnement manquante : " + name);
        }
        return value;
    }

    public String connectionInfo() {
        // On ne journalise jamais le secret lui-même.
        return "Connexion à " + url + " en tant que " + user
                + " (mot de passe de " + password.length + " caractères)";
    }

    public void close() {
        Arrays.fill(password, '\0'); // effacement best-effort en mémoire
    }

    public static void main(String[] args) {
        SecureDbClient client = new SecureDbClient();
        try {
            System.out.println(client.connectionInfo());
        } finally {
            client.close();
        }
    }
}
