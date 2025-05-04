package puiiiokiq.anicat.backend.anime;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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

        // Подготовка: разбиваем строку, фильтруем короткие слова
        String[] queryWords = Arrays.stream(query.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(word -> word.length() >= 3) // ⬅️ ищем только по словам длиной 3+ символа
                .toArray(String[]::new);

        if (queryWords.length == 0) {
            return ResponseEntity.badRequest().body("Введите более осмысленный запрос (от 3 символов)");
        }

        List<Anime> results = animeRepository.findAll().stream()
                .filter(anime -> {
                    String title = anime.getTitle() != null ? anime.getTitle().toLowerCase(Locale.ROOT) : "";
                    String altTitle = anime.getAlttitle() != null ? anime.getAlttitle().toLowerCase(Locale.ROOT) : "";

                    // Ищем совпадение по словам (точное вхождение)
                    for (String word : queryWords) {
                        List<String> titleWords = Arrays.asList(title.split("\\s+"));
                        List<String> altWords = Arrays.asList(altTitle.split("\\s+"));

                        if (titleWords.contains(word) || altWords.contains(word)) {
                            return true;
                        }
                    }

                    return false;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

}
