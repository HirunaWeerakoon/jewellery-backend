import org.springframework.boot.SpringBootVersion;

public class AppVersion {
    public static void main(String[] args) {
        System.out.println("Spring Boot version: " + SpringBootVersion.getVersion());
    }
}
