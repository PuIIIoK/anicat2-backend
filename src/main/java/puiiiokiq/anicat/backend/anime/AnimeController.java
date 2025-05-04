
package puiiiokiq.anicat.backend.anime;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.episodes.Audio;
import puiiiokiq.anicat.backend.episodes.AudioRepository;
import puiiiokiq.anicat.backend.episodes.Episode;
import puiiiokiq.anicat.backend.episodes.EpisodeRepository;
import puiiiokiq.anicat.backend.utils.service.S3Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class AnimeController {

    private final AnimeRepository animeRepository;
    private final EpisodeRepository episodeRepository;
    private final AudioRepository audioRepository;
    private final ScreenshotsRepository screenshotsRepository;
    private final CoverRepository coverRepository;
    private final S3Service s3Service;

    @GetMapping("/get-anime")
    public List<Anime> getAllAnime() {
        return animeRepository.findAll();
    }

    @GetMapping("/get-anime/{id}")
    public ResponseEntity<AnimeDTO> getAnimeById(@PathVariable Long id) {
        return animeRepository.findById(id)
                .map(anime -> {
                    String imageUrl = "/anime-cover-default.jpg";
                    if (anime.getCover() != null && anime.getCover().getId() != null) {
                        imageUrl = s3Service.generatePresignedCoverUrl(anime.getId(), anime.getCover().getId());
                    }
                    AnimeDTO dto = new AnimeDTO(anime, imageUrl);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/get-anime/{id}/episodes")
    public ResponseEntity<List<Episode>> getEpisodesByAnimeId(@PathVariable Long id) {
        return animeRepository.findById(id)
                .map(anime -> ResponseEntity.ok(anime.getEpisodes()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/get-anime/{animeId}/episodes/{episodeId}")
    public ResponseEntity<Episode> getEpisodeByAnimeAndId(
            @PathVariable Long animeId,
            @PathVariable Long episodeId) {
        Optional<Episode> episodeOpt = episodeRepository.findById(episodeId);
        if (episodeOpt.isPresent()) {
            Episode episode = episodeOpt.get();
            if (episode.getAnime().getId().equals(animeId)) {
                return ResponseEntity.ok(episode);
            }
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/get-anime/{animeId}/episodes/{episodeId}/language")
    public ResponseEntity<List<Audio>> getLanguagesForEpisode(
            @PathVariable Long animeId,
            @PathVariable Long episodeId) {
        Optional<Episode> episodeOpt = episodeRepository.findById(episodeId);
        if (episodeOpt.isPresent() && episodeOpt.get().getAnime().getId().equals(animeId)) {
            return ResponseEntity.ok(episodeOpt.get().getAudios());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/get-anime/{animeId}/episodes/{episodeId}/language/{languageId}")
    public ResponseEntity<Audio> getAudioInfo(
            @PathVariable Long animeId,
            @PathVariable Long episodeId,
            @PathVariable Long languageId) {
        Optional<Audio> audioOpt = audioRepository.findById(languageId);
        if (audioOpt.isPresent()) {
            Audio audio = audioOpt.get();
            if (audio.getAnime().getId().equals(animeId) &&
                audio.getEpisode().getId().equals(episodeId)) {
                return ResponseEntity.ok(audio);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/get-anime/{animeId}/screenshots")
    public ResponseEntity<List<Screenshots>> getScreenshots(@PathVariable Long animeId) {
        List<Screenshots> screenshots = screenshotsRepository.findByAnimeId(animeId);
        if (screenshots.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(screenshots);
    }

    @GetMapping("/get-anime/{animeId}/screenshots/{screenshotId}")
    public ResponseEntity<Screenshots> getScreenshotById(
            @PathVariable Long animeId,
            @PathVariable Long screenshotId) {
        Optional<Screenshots> screenshotOpt = screenshotsRepository.findById(screenshotId);
        if (screenshotOpt.isEmpty()) return ResponseEntity.notFound().build();

        Screenshots screenshot = screenshotOpt.get();
        if (!screenshot.getAnime().getId().equals(animeId)) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(screenshot);
    }

    @GetMapping("/get-anime/{animeId}/cover/{coverId}")
    public ResponseEntity<Cover> getCoverById(
            @PathVariable Long animeId,
            @PathVariable Long coverId) {
        Optional<Cover> coverOpt = coverRepository.findById(coverId);
        if (coverOpt.isEmpty()) return ResponseEntity.notFound().build();

        Cover cover = coverOpt.get();
        if (!cover.getAnime().getId().equals(animeId)) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(cover);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAnimeById(@PathVariable Long id) {
        Optional<Anime> optionalAnime = animeRepository.findById(id);

        if (optionalAnime.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Аниме не найдено");
        }

        Anime anime = optionalAnime.get();

        // 1. Удаление эпизодов и озвучек
        List<Episode> episodes = episodeRepository.findByAnimeId(anime.getId());
        for (Episode episode : episodes) {
            audioRepository.deleteAll(audioRepository.findByEpisodeId(episode.getId()));
        }

        episodeRepository.deleteAll(episodes);

        // 2. Удаление скриншотов
        screenshotsRepository.deleteAll(screenshotsRepository.findByAnimeId(anime.getId()));

        // 3. Удаление обложек
        coverRepository.deleteAll(coverRepository.findByAnimeId(anime.getId()));

        // 4. Удаление самого аниме
        animeRepository.delete(anime);

        return ResponseEntity.ok("✅ Аниме и все связанные данные успешно удалены.");
    }
}
