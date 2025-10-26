package gemshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication   // scans gemshop.* by default
public class GemshopApplication {
    public static void main(String[] args) {
        SpringApplication.run(GemshopApplication.class, args);
    }
}
