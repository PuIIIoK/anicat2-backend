package puiiiokiq.anicat.backend.episodes.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.episodes.AnimeEpisodeService;
import puiiiokiq.anicat.backend.episodes.models.Audio;
import puiiiokiq.anicat.backend.episodes.models.Episode;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class AnimeEpisodeController {

    private final AnimeEpisodeService service;

    @GetMapping("/{animeId}/episodes")
    public ResponseEntity<List<Episode>> getEpisodes(@PathVariable Long animeId) {
        return ResponseEntity.ok(service.getEpisodesByAnimeId(animeId));
    }

    @GetMapping("/{animeId}/audios")
    public ResponseEntity<List<Audio>> getAudios(@PathVariable Long animeId) {
        return ResponseEntity.ok(service.getAudiosByAnimeId(animeId));
    }

    @GetMapping("/{animeId}/audios/count")
    public ResponseEntity<Integer> getAudioCount(@PathVariable Long animeId) {
        return ResponseEntity.ok(service.getAudiosByAnimeId(animeId).size());
    }

    @GetMapping("/{animeId}/info")
    public ResponseEntity<Map<String, Object>> getFullInfo(@PathVariable Long animeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("episodes", service.getEpisodesByAnimeId(animeId));
        result.put("audios", service.getAudiosByAnimeId(animeId));
        return ResponseEntity.ok(result);
    }
}