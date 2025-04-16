package puiiiokiq.anicat.backend.profiles;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import puiiiokiq.anicat.backend.utils.service.S3Service;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class SettingsController {

    private final S3Service s3Service;
    private final String bucketName = "anicat2";

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
            String extension = getExtension(file.getOriginalFilename());
            String s3Key = String.format("profile/static/%s/%s.%s", type, fileId, extension);

            s3Service.uploadFile(bucketName, s3Key, file); // <-- Вот тут

            return ResponseEntity.ok().body("Файл успешно загружен: " + s3Key);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка загрузки: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
