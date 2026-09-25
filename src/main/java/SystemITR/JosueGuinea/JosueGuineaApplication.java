package SystemITR.JosueGuinea;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JosueGuineaApplication {

	public static void main(String[] args) {
        // Carga el .env solo localmente. Si no existe (como en Heroku), lo ignora sin fallar.
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        dotenv.entries().forEach(entry -> {
            // Solo asigna a System Property si no ha sido definida por el entorno real del servidor
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        // Arrancar la API
        SpringApplication.run(JosueGuineaApplication.class, args);
	}
}
