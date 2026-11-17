import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Preuve vérifiable (sans dépendance JUnit) :
// on lit les octets des .class compilés et on cherche le secret dedans.
// Lancer : java SecretLeakTest   (après javac *.java)
public class SecretLeakTest {

    // Construit à l'exécution (String.join n'est pas une constante de compilation) :
    // sinon javac mettrait la chaîne entière dans SecretLeakTest.class et le test
    // lui-même contiendrait le secret qu'il cherche.
    static final String SECRET = String.join("", "SuperSecret_", "Prod_2026!");

    static boolean classContainsSecret(String className) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(className + ".class"));
        return new String(bytes, StandardCharsets.ISO_8859_1).contains(SECRET);
    }

    public static void main(String[] args) throws Exception {
        boolean vulnLeaks = classContainsSecret("VulnerableDbClient");
        boolean secureLeaks = classContainsSecret("SecureDbClient");
        boolean testLeaks = classContainsSecret("SecretLeakTest");

        System.out.println("VulnerableDbClient.class contient le secret : " + vulnLeaks);
        System.out.println("SecureDbClient.class contient le secret     : " + secureLeaks);

        if (!vulnLeaks) throw new AssertionError("La version vulnérable devrait fuiter");
        if (secureLeaks) throw new AssertionError("La version corrigée ne doit pas fuiter");
        if (testLeaks) throw new AssertionError("Le test ne doit pas contenir le secret entier");

        // Le client corrigé refuse de démarrer sans secret fourni.
        // On le lance dans un processus séparé avec un environnement nettoyé,
        // pour que le test soit fiable même si DB_* existe dans le shell.
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        ProcessBuilder pb = new ProcessBuilder(java, "-cp", ".", "SecureDbClient")
                .redirectErrorStream(true);
        pb.environment().keySet().removeIf(k -> k.startsWith("DB_"));
        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = p.waitFor();
        if (code == 0 || !out.contains("manquante")) {
            throw new AssertionError("Aurait dû refuser de démarrer sans DB_* :\n" + out);
        }
        System.out.println("Sans variable d'env -> refus de démarrer (code " + code + ")");
        System.out.println("OK : tous les tests passent");
    }
}
