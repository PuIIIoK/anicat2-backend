package puiiiokiq.anicat.backend.profiles.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.profiles.Repository.ProfileRepository;
import puiiiokiq.anicat.backend.profiles.Repository.UserRepository;
import puiiiokiq.anicat.backend.profiles.models.Profile;
import puiiiokiq.anicat.backend.utils.service.S3Service;

import java.net.URL;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final S3Service s3Service;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    private final List<String> extensions = List.of("webp", "jpg", "jpeg", "png");

    @GetMapping("/get-cover-lk")
    public ResponseEntity<?> getCoverFromToken(Authentication auth) {
        String username = auth.getName();
        Long profileId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"))
                .getProfile().getId();

        return getImageByType(profileId, "cover");
    }

    @GetMapping("/get-banner-lk")
    public ResponseEntity<?> getBannerFromToken(Authentication auth) {
        String username = auth.getName();
        Long profileId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"))
                .getProfile().getId();

        return getImageByType(profileId, "banner");
    }

    @GetMapping("/get-cover-id/{profileId}/cover/{coverId}")
    public ResponseEntity<?> getCoverById(@PathVariable Long profileId, @PathVariable String coverId) {
        return getImageByFileId(profileId, coverId, "cover");
    }

    @GetMapping("/get-banner-id/{profileId}/banner/{bannerId}")
    public ResponseEntity<?> getBannerById(@PathVariable Long profileId, @PathVariable String bannerId) {
        return getImageByFileId(profileId, bannerId, "banner");
    }

    private ResponseEntity<?> getImageByType(Long profileId, String type) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Профиль с ID %d не найден".formatted(profileId)));

        String fileId = switch (type) {
            case "cover" -> profile.getAvatarId();
            case "banner" -> profile.getBannerId();
            default -> throw new RuntimeException("Неверный тип изображения: " + type);
        };

        return findFileInS3(fileId, type);
    }

    private ResponseEntity<?> getImageByFileId(Long profileId, String fileId, String type) {
        if (!profileRepository.existsById(profileId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Профиль с ID %d не найден".formatted(profileId)));
        }
        return findFileInS3(fileId, type);
    }

    private ResponseEntity<?> findFileInS3(String fileId, String type) {
        if (fileId == null || fileId.isBlank()) {
            return ResponseEntity.status(404).body(Map.of("error", "ID файла для %s отсутствует".formatted(type)));
        }

        for (String ext : extensions) {
            String s3Key = "profile/static/%s/%s.%s".formatted(type, fileId, ext);
            if (s3Service.fileExists(s3Key)) {
                URL url = s3Service.generatePresignedUrl(s3Key);
                return ResponseEntity.ok(Map.of("url", url.toString()));
            }
        }

        return ResponseEntity.status(404).body(Map.of("error", "Файл не найден для %s с ID %s".formatted(type, fileId)));
    }
}
