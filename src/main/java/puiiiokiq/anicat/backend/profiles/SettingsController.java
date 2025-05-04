package puiiiokiq.anicat.backend.profiles;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import puiiiokiq.anicat.backend.utils.service.S3Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@RestController
@RequestMapping("/api/upload")
public class SettingsController {

    private final S3Service s3Service;

    public SettingsController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type, // "cover" или "banner"
            @RequestParam("id") String fileId
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл пуст");
        }

        try {
            // Просто задаём новое имя файла с .webp
            String s3Key = String.format("profile/static/%s/%s.webp", type, fileId);

            // Загружаем оригинальный файл без конвертации
            s3Service.uploadFile(s3Key, file);

            return ResponseEntity.ok("Файл загружен как .webp: " + s3Key);
        } catch (Exception e) {
            e.printStackTrace(); // для отладки
            return ResponseEntity.internalServerError().body("Ошибка загрузки: " + e.getMessage());
        }
    }
}
