package puiiiokiq.anicat.backend.episodes.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.anime.Repository.AnimeRepository;
import puiiiokiq.anicat.backend.anime.Repository.BannerRepository;
import puiiiokiq.anicat.backend.anime.Repository.CoverRepository;
import puiiiokiq.anicat.backend.anime.Repository.ScreenshotsRepository;
import puiiiokiq.anicat.backend.anime.models.Anime;
import puiiiokiq.anicat.backend.anime.models.Banner;
import puiiiokiq.anicat.backend.anime.models.Cover;
import puiiiokiq.anicat.backend.anime.models.Screenshots;
import puiiiokiq.anicat.backend.episodes.Repository.AudioRepository;
import puiiiokiq.anicat.backend.episodes.Repository.EpisodeRepository;
import puiiiokiq.anicat.backend.episodes.models.Audio;
import puiiiokiq.anicat.backend.episodes.models.Episode;
import puiiiokiq.anicat.backend.utils.service.S3Service;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamingController {

    private final AnimeRepository animeRepository;
    private final EpisodeRepository episodeRepository;
    private final AudioRepository audioRepository;
    private final S3Service s3Service;
    private final CoverRepository coverRepository;
    private final ScreenshotsRepository screenshotsRepository;
    private final BannerRepository bannerRepository;

    @GetMapping("/anime/{animeId}/episode/{episodeId}/audio-name/{audioName}")
    public ResponseEntity<?> streamAudioByName(
            @PathVariable Long animeId,
            @PathVariable Long episodeId,
            @PathVariable String audioName) {

        if (animeRepository.findById(animeId).isEmpty()) {
            return ResponseEntity.badRequest().body("Аниме с ID " + animeId + " не найдено.");
        }

        Optional<Episode> episodeOpt = episodeRepository.findById(episodeId);
        if (episodeOpt.isEmpty() || !episodeOpt.get().getAnime().getId().equals(animeId)) {
            return ResponseEntity.badRequest().body("Эпизод с ID " + episodeId + " не найден у указанного аниме.");
        }

        Optional<Audio> audioOpt = audioRepository.findAll().stream()
                .filter(a -> a.getAnime().getId().equals(animeId)
                        && a.getEpisode().getId().equals(episodeId)
                        && a.getName().equalsIgnoreCase(audioName))
                .findFirst();

        if (audioOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Озвучка с названием '" + audioName + "' не найдена для этого эпизода.");
        }

        String sanitizedAudioName = audioOpt.get().getName().replace(" ", "");
        String s3Key = String.format("animes/%d/episodes/%s/1080/%d.mp4", animeId, sanitizedAudioName, episodeId);

        try {
            if (!s3Service.fileExists(s3Key)) {
                return ResponseEntity.status(404).body("Файл не найден в S3-хранилище.");
            }

            URL url = s3Service.generatePresignedUrl(s3Key);
            return ResponseEntity.ok(Map.of("url", url.toString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении ссылки на видео.");
        }
    }

    @GetMapping("/anime/{animeId}/cover/{coverId}")
    public ResponseEntity<?> getCoverFromS3(@PathVariable Long animeId, @PathVariable Long coverId) {
        if (animeRepository.findById(animeId).isEmpty()) {
            return ResponseEntity.badRequest().body("Аниме с ID " + animeId + " не найдено.");
        }

        Optional<Cover> coverOpt = coverRepository.findById(coverId);
        if (coverOpt.isEmpty() || !coverOpt.get().getAnime().getId().equals(animeId)) {
            return ResponseEntity.badRequest().body("Обложка с ID " + coverId + " не найдена для указанного аниме.");
        }

        String s3Key = String.format("animes/%d/cover/%d.webp", animeId, coverId);

        try {
            if (!s3Service.fileExists(s3Key)) {
                return ResponseEntity.status(404).body("Обложка не найдена в S3-хранилище.");
            }

            URL url = s3Service.generatePresignedUrl(s3Key);
            return ResponseEntity.ok(Map.of("url", url.toString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении обложки.");
        }
    }

    @GetMapping("/anime/{animeId}/screenshots/{screenshotId}")
    public ResponseEntity<?> getScreenshotFromS3(@PathVariable Long animeId, @PathVariable Long screenshotId) {
        if (animeRepository.findById(animeId).isEmpty()) {
            return ResponseEntity.badRequest().body("Аниме с ID " + animeId + " не найдено.");
        }

        Optional<Screenshots> screenshotOpt = screenshotsRepository.findById(screenshotId);
        if (screenshotOpt.isEmpty() || !screenshotOpt.get().getAnime().getId().equals(animeId)) {
            return ResponseEntity.badRequest().body("Скриншот с ID " + screenshotId + " не найден для указанного аниме.");
        }

        String s3Key = String.format("animes/%d/screenshots/%d.webp", animeId, screenshotId);

        try {
            if (!s3Service.fileExists(s3Key)) {
                return ResponseEntity.status(404).body("Скриншот не найден в S3-хранилище.");
            }

            URL url = s3Service.generatePresignedUrl(s3Key);
            return ResponseEntity.ok(Map.of("url", url.toString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении скриншота.");
        }
    }

    @GetMapping("/anime/{animeId}/screenshots")
    public ResponseEntity<?> getAllScreenshotsFromS3(@PathVariable Long animeId) {
        if (animeRepository.findById(animeId).isEmpty()) {
            return ResponseEntity.badRequest().body("Аниме с ID " + animeId + " не найдено.");
        }

        List<Screenshots> screenshots = screenshotsRepository.findAll().stream()
                .filter(s -> s.getAnime().getId().equals(animeId))
                .toList();

        if (screenshots.isEmpty()) {
            return ResponseEntity.status(404).body("Скриншоты не найдены для аниме с ID " + animeId);
        }

        try {
            List<Map<String, String>> screenshotLinks = screenshots.stream()
                    .map(screenshot -> {
                        String s3Key = String.format("animes/%d/screenshots/%d.webp", animeId, screenshot.getId());

                        if (!s3Service.fileExists(s3Key)) {
                            return Map.of(
                                    "id", String.valueOf(screenshot.getId()),
                                    "status", "not_found"
                            );
                        }

                        URL url = s3Service.generatePresignedUrl(s3Key);
                        return Map.of(
                                "id", String.valueOf(screenshot.getId()),
                                "url", url.toString()
                        );
                    })
                    .toList();

            return ResponseEntity.ok(screenshotLinks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при генерации ссылок на скриншоты.");
        }
    }

    @GetMapping("/{animeId}/cover")
    public ResponseEntity<?> getCover(@PathVariable Long animeId) {
        List<Cover> covers = coverRepository.findByAnimeId(animeId);
        if (covers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Обложка не найдена");
        }

        Cover cover = covers.get(0);
        String key = "animes/" + animeId + "/cover/" + cover.getId() + ".webp";

        try {
            var stream = s3Service.getFileStream(key);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/webp"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + cover.getId() + ".webp\"")
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка загрузки обложки");
        }
    }

    @GetMapping("/{animeId}/banner/{bannerId}")
    public ResponseEntity<?> getBannerUrlById(@PathVariable Long animeId, @PathVariable Long bannerId) {
        Optional<Banner> bannerOpt = bannerRepository.findById(bannerId);
        if (bannerOpt.isEmpty() || !bannerOpt.get().getAnime().getId().equals(animeId)) {
            return ResponseEntity.badRequest().body("Баннер не найден или не принадлежит этому аниме");
        }

        String s3Key = String.format("animes/%d/banner/%d.webp", animeId, bannerId);

        try {
            if (!s3Service.fileExists(s3Key)) {
                return ResponseEntity.status(404).body("Баннер не найден в S3-хранилище.");
            }

            URL url = s3Service.generatePresignedUrl(s3Key);
            return ResponseEntity.ok(Map.of("url", url.toString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении ссылки на баннер.");
        }
    }

    // ✅ 2. Получение ссылки на первый баннер по ID аниме
    @GetMapping("/{animeId}/banner")
    public ResponseEntity<?> getBannerUrlByAnimeId(@PathVariable Long animeId) {
        Optional<Anime> animeOpt = animeRepository.findById(animeId);
        if (animeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Аниме не найдено");
        }

        List<Banner> banners = bannerRepository.findByAnimeId(animeId);
        if (banners.isEmpty()) {
            return ResponseEntity.status(404).body("Баннеров нет для этого аниме");
        }

        Banner banner = banners.get(0);
        String s3Key = String.format("animes/%d/banner/%d.webp", animeId, banner.getId());

        try {
            if (!s3Service.fileExists(s3Key)) {
                return ResponseEntity.status(404).body("Баннер не найден в S3-хранилище.");
            }

            URL url = s3Service.generatePresignedUrl(s3Key);
            return ResponseEntity.ok(Map.of("url", url.toString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении баннера.");
        }
    }

    // ✅ 3. Прямой поток баннера по ID
    @GetMapping("/{animeId}/banner-direct")
    public ResponseEntity<?> getBannerDirectStream(@PathVariable Long animeId) {
        List<Banner> banners = bannerRepository.findByAnimeId(animeId);
        if (banners.isEmpty()) {
            return ResponseEntity.status(404).body("Баннер не найден");
        }

        Banner banner = banners.get(0);
        String key = String.format("animes/%d/banner/%d.webp", animeId, banner.getId());

        try {
            var stream = s3Service.getFileStream(key);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/webp"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + banner.getId() + ".webp\"")
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при потоке баннера");
        }
    }
}
