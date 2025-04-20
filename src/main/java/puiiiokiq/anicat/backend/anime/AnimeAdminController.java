package puiiiokiq.anicat.backend.anime;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import puiiiokiq.anicat.backend.utils.service.S3Service;
import puiiiokiq.anicat.backend.episodes.Episode;
import puiiiokiq.anicat.backend.episodes.Audio;


import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import puiiiokiq.anicat.backend.anime.AnimeRepository;
import puiiiokiq.anicat.backend.episodes.EpisodeRepository;
import puiiiokiq.anicat.backend.episodes.AudioRepository;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AnimeAdminController {

    private final AnimeService animeService;
    private final S3Service s3Service;
    private final AnimeRepository animeRepository;
    private final EpisodeRepository episodeRepository;
    private final AudioRepository audioRepository;


    // 1. СОЗДАНИЕ ПУСТОЙ ЗАПИСИ И ПАПОК
    @PostMapping("/create-anime")
    public ResponseEntity<Long> createAnimeStructure() {
        Long animeId = animeService.createAnimeAndReturnId();
        s3Service.createAnimeDirectories(animeId);
        return ResponseEntity.ok(animeId);
    }

    // 2. УДАЛЕНИЕ АНИМЕ И ВСЕХ СВЯЗАННЫХ ДАННЫХ
    @DeleteMapping("/delete-anime/{animeId}")
    public ResponseEntity<String> deleteAnime(@PathVariable Long animeId) {
        animeService.deleteAnimeWithRelations(animeId);
        s3Service.deleteAnimeDirectory(animeId);
        return ResponseEntity.ok("Аниме и связанные данные удалены");
    }

    // 3. ЗАГРУЗКА ОБЛОЖКИ
    @PostMapping("/upload-cover/{animeId}")
    public ResponseEntity<String> uploadCover(
            @PathVariable Long animeId,
            @RequestParam("file") MultipartFile file
    ) {
        Long coverId = animeService.createCoverRecord(animeId);
        String newFilename = coverId + ".webp";
        s3Service.uploadFile("animes/" + animeId + "/cover/" + newFilename, file);
        return ResponseEntity.ok("Обложка загружена");
    }

    // 4. ЗАГРУЗКА ИНФОРМАЦИИ О АНИМЕ
    @PostMapping("/upload-info/{animeId}")
    public ResponseEntity<String> uploadAnimeInfo(
            @PathVariable Long animeId,
            @RequestBody AnimeInfoRequest request
    ) {
        animeService.saveAnimeInfo(animeId, request);
        return ResponseEntity.ok("Информация о аниме сохранена");
    }

    // 5. ЗАГРУЗКА СКРИНШОТОВ
    @PostMapping("/upload-screenshots/{animeId}")
    public ResponseEntity<String> uploadScreenshots(
            @PathVariable Long animeId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        if (files.size() > 4) {
            return ResponseEntity.badRequest().body("Максимум 4 скриншота");
        }

        for (MultipartFile file : files) {
            Long screenshotId = animeService.createScreenshotRecord(animeId);
            String filename = screenshotId + ".webp";
            s3Service.uploadFile("animes/" + animeId + "/screenshots/" + filename, file);
        }
        return ResponseEntity.ok("Скриншоты загружены");
    }

    // 6. ЗАГРУЗКА ЭПИЗОДОВ С ОЗВУЧКОЙ
    @PostMapping("/upload-episode/{animeId}/{audioName}/1080/{episodeId}")
    public ResponseEntity<String> uploadEpisodeToSpecific(
            @PathVariable Long animeId,
            @PathVariable String audioName,
            @PathVariable Long episodeId,
            @RequestParam("file") MultipartFile file
    ) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new RuntimeException("Аниме не найдено"));

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Эпизод не найден"));

        // 🔍 Проверяем, существует ли уже озвучка с таким названием для этого эпизода
        Optional<Audio> existingAudio = audioRepository.findByEpisodeIdAndName(episodeId, audioName);

        if (existingAudio.isEmpty()) {
            Audio audio = new Audio();
            audio.setName(audioName);
            audio.setAnime(anime);
            audio.setEpisode(episode);
            audioRepository.save(audio);
        }

        // 📁 Задаём путь с нужным расширением .mp4
        String path = String.format("animes/%d/episodes/%s/1080/%d.mp4", animeId, audioName, episodeId);

        // 📤 Загружаем файл, переименованный в .mp4
        s3Service.uploadFile(path, file);

        return ResponseEntity.ok("Файл загружен как .mp4 для эпизода " + episodeId);
    }
    @PostMapping("/get-upload-url")
    public ResponseEntity<String> getUploadUrl(
            @RequestBody UploadRequest request
    ) {
        String key = String.format("animes/%d/episodes/%s/1080/%d.mp4",
                request.getAnimeId(), request.getAudioName(), request.getEpisodeId());

        URL url = s3Service.generatePresignedUploadUrl(key);
        return ResponseEntity.ok(url.toString());
    }



    // 7. ПРОВЕРКА УСПЕШНОСТИ ЗАГРУЗКИ
    @GetMapping("/check-upload/{animeId}")
    public ResponseEntity<String> checkUpload(@PathVariable Long animeId) {
        // Проверка только основной информации и обложки (без эпизодов, аудио и alias)
        boolean result = animeService.checkAnimeUpload(animeId);

        if (result) {
            return ResponseEntity.ok("✅ Аниме успешно загружено. Возврат в админ-панель...");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ Не все данные загружены. Проверь загрузки (обложка и основная информация).");
        }
    }

    // 8. ДОБАВЛЕНИЕ НАЗВАНИЕ ДЛЯ ЭПИЗОДА

    @PostMapping("/upload-episode/create-episode/{animeId}")
    public ResponseEntity<Map<String, Object>> createEpisode(
            @PathVariable Long animeId,
            @RequestBody Map<String, Object> payload
    ) {
        String title = Optional.ofNullable(payload.get("title"))
                .map(Object::toString)
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .orElse("Новый эпизод");

        Long episodeId = animeService.createEpisodeRecord(animeId, title);

        return ResponseEntity.ok(Map.of(
                "id", episodeId,
                "title", title
        ));
    }
    @PostMapping("/upload-episode/create-audio/{animeId}/{episodeId}")
    public ResponseEntity<Map<String, Object>> createAudio(
            @PathVariable Long animeId,
            @PathVariable Long episodeId,
            @RequestBody Map<String, Object> payload
    ) {
        String name = Optional.ofNullable(payload.get("name"))
                .map(Object::toString)
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .orElse("Новая озвучка");

        Anime anime = animeRepository.findById(animeId).orElseThrow();
        Episode episode = episodeRepository.findById(episodeId).orElseThrow();

        Audio audio = new Audio();
        audio.setAnime(anime);
        audio.setEpisode(episode);
        audio.setName(name);

        audioRepository.save(audio);

        return ResponseEntity.ok(Map.of("id", audio.getId(), "name", audio.getName()));
    }

    @PutMapping("/upload-episode/update-episode/{episodeId}")
    public ResponseEntity<String> updateEpisodeTitle(
            @PathVariable Long episodeId,
            @RequestBody Map<String, String> request
    ) {
        String newTitle = request.getOrDefault("title", "").trim();
        if (newTitle.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Название не может быть пустым");
        }

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("❌ Эпизод не найден"));

        episode.setTitle(newTitle);
        episodeRepository.save(episode);

        return ResponseEntity.ok("✅ Название эпизода обновлено");
    }

    @PutMapping("/upload-episode/update-audio/{audioId}")
    public ResponseEntity<String> updateAudioName(
            @PathVariable Long audioId,
            @RequestBody Map<String, String> request
    ) {
        String newName = request.getOrDefault("name", "").trim();
        if (newName.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Название озвучки не может быть пустым");
        }

        Audio audio = audioRepository.findById(audioId)
                .orElseThrow(() -> new RuntimeException("❌ Озвучка не найдена"));

        audio.setName(newName);
        audioRepository.save(audio);

        return ResponseEntity.ok("✅ Название озвучки обновлено");
    }

    @DeleteMapping("/delete-episode/{episodeId}")
    public ResponseEntity<String> deleteEpisode(@PathVariable Long episodeId) {
        var episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Эпизод не найден"));

        episode.getAudios().forEach(audio -> {
            Long animeId = episode.getAnime().getId();
            String audioName = audio.getName();

            String fileKey = String.format("animes/%d/episodes/%s/1080/%d.mp4", animeId, audioName, episodeId);
            s3Service.deleteFile(fileKey);

            String folderKey = String.format("animes/%d/episodes/%s/", animeId, audioName);
            s3Service.deleteFolderRecursive(folderKey);

            audioRepository.delete(audio);
        });

        episodeRepository.delete(episode);
        return ResponseEntity.ok("Эпизод и все озвучки удалены");
    }


    @DeleteMapping("/delete-audio/{audioId}")
    public ResponseEntity<String> deleteAudio(@PathVariable Long audioId) {
        var audio = audioRepository.findById(audioId)
                .orElseThrow(() -> new RuntimeException("Озвучка не найдена"));

        Long animeId = audio.getAnime().getId();
        Long episodeId = audio.getEpisode().getId();
        String audioName = audio.getName();

        String fileKey = String.format("animes/%d/episodes/%s/1080/%d.mp4", animeId, audioName, episodeId);
        s3Service.deleteFile(fileKey);

        String folderKey = String.format("animes/%d/episodes/%s/", animeId, audioName);
        s3Service.deleteFolderRecursive(folderKey);

        audioRepository.delete(audio);

        return ResponseEntity.ok("Озвучка и файл удалены");
    }


}
