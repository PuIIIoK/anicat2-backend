
package puiiiokiq.anicat.backend.anime.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.anime.Repository.AnimeRepository;
import puiiiokiq.anicat.backend.anime.Repository.CoverRepository;
import puiiiokiq.anicat.backend.anime.Service.AnimeAccessService;
import puiiiokiq.anicat.backend.anime.models.Screenshots;
import puiiiokiq.anicat.backend.anime.Repository.ScreenshotsRepository;
import puiiiokiq.anicat.backend.anime.models.Anime;
import puiiiokiq.anicat.backend.anime.models.AnimeDTO;
import puiiiokiq.anicat.backend.anime.models.Cover;
import puiiiokiq.anicat.backend.episodes.models.Audio;
import puiiiokiq.anicat.backend.episodes.Repository.AudioRepository;
import puiiiokiq.anicat.backend.episodes.models.Episode;
import puiiiokiq.anicat.backend.episodes.Repository.EpisodeRepository;
import puiiiokiq.anicat.backend.utils.service.S3Service;
import puiiiokiq.anicat.backend.anime.models.Banner;
import puiiiokiq.anicat.backend.anime.Repository.BannerRepository;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final AnimeAccessService animeAccessService;
    private final BannerRepository bannerRepository;

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

                    AnimeDTO dto = AnimeDTO.from(anime, imageUrl);
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


    @GetMapping("/get-anime/{animeId}/availability")
    public Map<String, Object> checkAnimeAvailability(@PathVariable Long animeId, HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        boolean isAccessible = animeAccessService.isAnimeAccessible(animeId, clientIp);

        Map<String, Object> response = new HashMap<>();
        response.put("accessible", isAccessible);
        response.put("ip", clientIp); // показать IP для отладки (можно убрать)

        return response;
    }



    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim(); // берём первый IP в списке
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        return request.getRemoteAddr();
    }



    @GetMapping("/get-anime/{animeId}/banner")
    public ResponseEntity<Map<String, Object>> getBannerIdByAnimeId(@PathVariable Long animeId) {
        Optional<Anime> animeOpt = animeRepository.findById(animeId);
        if (animeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Аниме не найдено"));
        }

        List<Banner> banners = bannerRepository.findByAnimeId(animeId);
        if (banners.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Баннер не найден"));
        }

        Banner banner = banners.get(0); // можно изменить на другой критерий

        Map<String, Object> response = new HashMap<>();
        response.put("bannerId", banner.getId());
        response.put("fileName", banner.getFileName());

        return ResponseEntity.ok(response);
    }



}
