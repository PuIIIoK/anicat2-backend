package puiiiokiq.anicat.backend.profiles.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import puiiiokiq.anicat.backend.profiles.Repository.ProfileRepository;
import puiiiokiq.anicat.backend.profiles.ReqResp.TestSettingsResponse;
import puiiiokiq.anicat.backend.profiles.Repository.UserRepository;
import puiiiokiq.anicat.backend.profiles.models.Profile;
import puiiiokiq.anicat.backend.profiles.models.User;
import puiiiokiq.anicat.backend.utils.service.S3Service;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class SettingsController {

    private final S3Service s3Service;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    /** Загрузка аватарки или баннера */
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
            String s3Key = String.format("profile/static/%s/%s.webp", type, fileId);
            s3Service.uploadFile(s3Key, file);
            return ResponseEntity.ok("Файл успешно загружен как .webp: " + s3Key);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка загрузки: " + e.getMessage());
        }
    }


    /** Получить тестовые настройки профиля */
    @PostMapping("/profile/test-settings")
    public ResponseEntity<?> updateTestSettings(
            Authentication authentication,
            @RequestBody TestSettingsResponse request
    ) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Profile profile = user.getProfile();
        if (profile == null) {
            return ResponseEntity.badRequest().body("Профиль не найден");
        }

        profile.setAnimePageBeta(request.isAnimePageBeta());
        profile.setProfilePageBeta(request.isProfilePageBeta());
        profileRepository.save(profile);

        return ResponseEntity.ok("Настройки успешно обновлены");
    }
}
