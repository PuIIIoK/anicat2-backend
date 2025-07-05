package puiiiokiq.anicat.backend.anime.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.anime.Repository.AnimeRepository;
import puiiiokiq.anicat.backend.anime.models.Anime;
import puiiiokiq.anicat.backend.anime.models.ProfileSearchDto;
import puiiiokiq.anicat.backend.profiles.Repository.ProfileRepository;
import puiiiokiq.anicat.backend.profiles.Repository.UserRepository;
import puiiiokiq.anicat.backend.profiles.models.Profile;
import puiiiokiq.anicat.backend.profiles.models.User;
import puiiiokiq.anicat.backend.utils.service.S3Service;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class AnimeSearchController {

    private final AnimeRepository animeRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final List<String> extensions = List.of("webp", "jpg", "jpeg", "png");

    @GetMapping("/search")
    public ResponseEntity<?> searchAnime(@RequestParam(value = "query", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Введите название аниме или ID");
        }

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        String[] queryWords = Arrays.stream(normalizedQuery.split("\\s+"))
                .filter(word -> word.length() >= 2)
                .toArray(String[]::new);

        Long idQuery = null;
        try {
            idQuery = Long.parseLong(query.trim());
        } catch (NumberFormatException ignored) {
        }

        Long finalIdQuery = idQuery;
        List<Anime> results = animeRepository.findAll().stream()
                .filter(anime -> {
                    boolean matchesTitle = false;

                    String title = anime.getTitle() != null ? anime.getTitle().toLowerCase(Locale.ROOT) : "";
                    String altTitle = anime.getAlttitle() != null ? anime.getAlttitle().toLowerCase(Locale.ROOT) : "";

                    for (String word : queryWords) {
                        List<String> titleWords = Arrays.asList(title.split("\\s+"));
                        List<String> altWords = Arrays.asList(altTitle.split("\\s+"));

                        if (titleWords.contains(word) || altWords.contains(word)) {
                            matchesTitle = true;
                            break;
                        }
                    }

                    boolean matchesId = finalIdQuery != null && anime.getId().equals(finalIdQuery);

                    return matchesTitle || matchesId;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }


    @GetMapping("/search-profiles")
    public ResponseEntity<?> searchProfiles(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Введите имя пользователя, ник или ID");
        }

        String lowered = query.trim().toLowerCase(Locale.ROOT);

        List<Profile> profiles = profileRepository.findAll().stream()
                .filter(profile -> {
                    String nickname = Optional.ofNullable(profile.getNickname()).orElse("").toLowerCase();
                    String idStr = String.valueOf(profile.getId());
                    return nickname.contains(lowered) || idStr.equals(lowered);
                })
                .collect(Collectors.toList());

        List<Profile> byUsername = userRepository.findAll().stream()
                .filter(user -> user.getUsername() != null && user.getUsername().toLowerCase().contains(lowered))
                .map(user -> profileRepository.findByUser(user).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        for (Profile profile : byUsername) {
            if (!profiles.contains(profile)) {
                profiles.add(profile);
            }
        }

        List<Map<String, Object>> result = profiles.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    User user = p.getUser();

                    map.put("id", p.getId());
                    map.put("username", user.getUsername());
                    map.put("nickname", p.getNickname());
                    map.put("bio", p.getBio());
                    map.put("avatarId", p.getAvatarId());
                    map.put("bannerId", p.getBannerId());

                    // Добавляем список ролей
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.name().replace("ROLE_", ""))
                            .toList();
                    map.put("roles", roles);

                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("profiles", result));
    }


    @GetMapping("/image-links")
    public ResponseEntity<?> getProfileImages(@RequestParam(required = false) Long id,
                                              @RequestParam(required = false) String nickname,
                                              @RequestParam(required = false) String username) {
        Optional<Profile> profileOpt = findProfile(id, nickname, username);

        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Профиль не найден"));
        }

        Profile profile = profileOpt.get();

        String avatarId = profile.getAvatarId();
        String bannerId = profile.getBannerId();

        String avatarUrl = getPresignedUrl(avatarId, "cover");
        String bannerUrl = getPresignedUrl(bannerId, "banner");

        Map<String, Object> result = new HashMap<>();
        result.put("avatarUrl", avatarUrl);
        result.put("bannerUrl", bannerUrl);
        return ResponseEntity.ok(result);
    }

    private Optional<Profile> findProfile(Long id, String nickname, String username) {
        if (id != null) return profileRepository.findById(id);
        if (nickname != null) return profileRepository.findByNickname(nickname);
        if (username != null) {
            return userRepository.findByUsername(username)
                    .flatMap(profileRepository::findByUser);
        }
        return Optional.empty();
    }

    private String getPresignedUrl(String fileId, String type) {
        if (fileId == null || fileId.isBlank()) return null;

        for (String ext : extensions) {
            String key = "profile/static/%s/%s.%s".formatted(type, fileId, ext);
            if (s3Service.fileExists(key)) {
                URL url = s3Service.generatePresignedUrl(key);
                return url.toString();
            }
        }

        return null;
    }

}