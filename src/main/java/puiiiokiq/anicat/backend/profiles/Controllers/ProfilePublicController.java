package puiiiokiq.anicat.backend.profiles.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.profiles.Repository.ProfileRepository;
import puiiiokiq.anicat.backend.profiles.Repository.UserRepository;
import puiiiokiq.anicat.backend.profiles.models.Profile;
import puiiiokiq.anicat.backend.profiles.models.User;
import puiiiokiq.anicat.backend.utils.service.S3Service;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfilePublicController {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    private final List<String> extensions = List.of("webp", "jpg", "png", "jpeg");

    // 1. Получить профиль по ID, nickname или username
    @GetMapping("/get-profile")
    public ResponseEntity<?> getProfile(@RequestParam(required = false) Long id,
                                        @RequestParam(required = false) String nickname,
                                        @RequestParam(required = false) String username) {

        Optional<Profile> profileOpt = Optional.empty();

        if (id != null) {
            profileOpt = profileRepository.findById(id);
        } else if (nickname != null) {
            profileOpt = profileRepository.findByNickname(nickname);
        } else if (username != null) {
            profileOpt = userRepository.findByUsername(username)
                    .flatMap(profileRepository::findByUser);
        }

        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Профиль не найден"));
        }

        Profile profile = profileOpt.get();
        User user = profile.getUser();

        Map<String, Object> result = new HashMap<>();
        result.put("profileId", profile.getId());
        result.put("nickname", profile.getNickname());
        result.put("bio", profile.getBio());
        result.put("avatarId", profile.getAvatarId());
        result.put("bannerId", profile.getBannerId());
        result.put("username", user.getUsername());

        // Безопасная обработка ролей
        if (user.getRoles() != null) {
            result.put("roles", user.getRoles().stream().map(Enum::name).toList());
        } else {
            result.put("roles", List.of()); // или можно не добавлять вовсе
        }

        return ResponseEntity.ok(result);
    }

    // 2. Получить аватарку по ID/nickname/username
    @GetMapping("/avatar")
    public ResponseEntity<?> getAvatar(@RequestParam(required = false) Long id,
                                       @RequestParam(required = false) String nickname,
                                       @RequestParam(required = false) String username) {
        return getImageGeneric(id, nickname, username, "cover");
    }

    // 3. Получить баннер по ID/nickname/username
    @GetMapping("/banner")
    public ResponseEntity<?> getBanner(@RequestParam(required = false) Long id,
                                       @RequestParam(required = false) String nickname,
                                       @RequestParam(required = false) String username) {
        return getImageGeneric(id, nickname, username, "banner");
    }

    // 4. Получить только описание (bio)
    @GetMapping("/bio")
    public ResponseEntity<?> getBio(@RequestParam(required = false) Long id,
                                    @RequestParam(required = false) String nickname,
                                    @RequestParam(required = false) String username) {
        Optional<Profile> profileOpt = findProfile(id, nickname, username);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Профиль не найден"));
        }

        return ResponseEntity.ok(Map.of("bio", profileOpt.get().getBio()));
    }

    // Вспомогательный метод для поиска профиля
    private Optional<Profile> findProfile(Long id, String nickname, String username) {
        if (id != null) return profileRepository.findById(id);
        if (nickname != null) return profileRepository.findByNickname(nickname);
        if (username != null) {
            return userRepository.findByUsername(username)
                    .flatMap(profileRepository::findByUser);
        }
        return Optional.empty();
    }

    // Вспомогательный метод для получения URL аватарки/баннера
    private ResponseEntity<?> getImageGeneric(Long id, String nickname, String username, String type) {
        Optional<Profile> profileOpt = findProfile(id, nickname, username);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Профиль не найден"));
        }

        Profile profile = profileOpt.get();
        String fileId = type.equals("cover") ? profile.getAvatarId() : profile.getBannerId();

        if (fileId == null || fileId.isBlank()) {
            return ResponseEntity.status(404).body(Map.of("error", "ID файла отсутствует"));
        }

        for (String ext : extensions) {
            String s3Key = "profile/static/%s/%s.%s".formatted(type, fileId, ext);
            if (s3Service.fileExists(s3Key)) {
                URL url = s3Service.generatePresignedUrl(s3Key);
                return ResponseEntity.ok(Map.of("url", url.toString()));
            }
        }

        return ResponseEntity.status(404).body(Map.of("error", "Файл не найден"));
    }
}
