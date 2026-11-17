// VULNÉRABLE : CWE-798 / CERT MSC03-J
// Le mot de passe est une constante dans le code source.
// Il finit dans le .class (constant pool), dans Git, dans chaque livrable.
public class VulnerableDbClient {

    private static final String DB_URL = "jdbc:postgresql://prod-db:5432/app";
    private static final String DB_USER = "app_admin";
    private static final String DB_PASSWORD = "SuperSecret_Prod_2026!";

    public String connectionInfo() {
        // En vrai : DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)
        return "Connexion à " + DB_URL + " en tant que " + DB_USER
                + " (mot de passe de " + DB_PASSWORD.length() + " caractères)";
    }

    public static void main(String[] args) {
        System.out.println(new VulnerableDbClient().connectionInfo());
    }
}
