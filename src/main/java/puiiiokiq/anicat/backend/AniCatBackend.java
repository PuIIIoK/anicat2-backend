package puiiiokiq.anicat.backend;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

@SpringBootApplication
public class AniCatBackend {
  public static void main(String[] args) {
    // Загружаем .env из корня проекта
    Dotenv dotenv = Dotenv.configure()
            .directory("./")       // путь до папки, где лежит .env
            .filename(".env")      // имя файла
            .load();

    // Устанавливаем переменные окружения в System
    dotenv.entries().forEach(entry ->
            System.setProperty(entry.getKey(), entry.getValue())
    );

    SpringApplication.run(AniCatBackend.class, args);
  }
}