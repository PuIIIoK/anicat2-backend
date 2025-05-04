package puiiiokiq.anicat.backend.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import puiiiokiq.anicat.backend.anime.models.Anime;
import puiiiokiq.anicat.backend.anime.Repository.AnimeRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/libria")
@RequiredArgsConstructor
public class LibriaController {

    private final AnimeRepository animeRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/episodes/{animeId}")
    public ResponseEntity<?> getEpisodesByAlias(@PathVariable Long animeId) {
        try {
            Optional<Anime> animeOptional = animeRepository.findById(animeId);
            if (animeOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Anime anime = animeOptional.get();
            String alias = anime.getAlias();

            if (alias == null || alias.isBlank()) {
                return ResponseEntity.badRequest().body("Алиас не задан для данного аниме");
            }

            String url = "https://anilibria.wtf/api/v1/anime/releases/" + alias;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(response.getStatusCode()).body("Ошибка при запросе AniLibria");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode episodes = root.path("episodes");

            if (episodes.isMissingNode() || !episodes.isArray()) {
                return ResponseEntity.status(500).body("Невозможно извлечь эпизоды из ответа");
            }

            return ResponseEntity.ok(episodes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }
}
