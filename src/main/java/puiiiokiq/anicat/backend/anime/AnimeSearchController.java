package puiiiokiq.anicat.backend.anime;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class AnimeSearchController {

    private final AnimeRepository animeRepository;

    @GetMapping("/search")
    public ResponseEntity<?> searchAnime(@RequestParam(value = "query", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Введите название аниме, которое хотите найти");
        }

        String[] queryWords = query.trim().toLowerCase(Locale.ROOT).split("\\s+");

        List<Anime> results = animeRepository.findAll().stream()
                .filter(anime -> {
                    String title = anime.getTitle().toLowerCase(Locale.ROOT);
                    String altTitle = anime.getAlttitle().toLowerCase(Locale.ROOT);

                    // Проверяем, что хотя бы одно слово из запроса содержится полностью
                    for (String word : queryWords) {
                        if (title.contains(" " + word + " ") || title.startsWith(word + " ") || title.endsWith(" " + word)
                                || altTitle.contains(" " + word + " ") || altTitle.startsWith(word + " ") || altTitle.endsWith(" " + word)) {
                            return true;
                        }
                    }

                    return false;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }
}
