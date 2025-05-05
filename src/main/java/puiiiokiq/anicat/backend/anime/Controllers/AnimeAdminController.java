package puiiiokiq.anicat.backend.anime.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import puiiiokiq.anicat.backend.anime.AnimeInfoRequest;
import puiiiokiq.anicat.backend.anime.Repository.AnimeRepository;
import puiiiokiq.anicat.backend.anime.Service.AnimeService;
import puiiiokiq.anicat.backend.anime.UploadRequest;
import puiiiokiq.anicat.backend.anime.models.Anime;
import puiiiokiq.anicat.backend.anime.models.Banner;
import puiiiokiq.anicat.backend.anime.models.Cover;
import puiiiokiq.anicat.backend.anime.models.Screenshots;
import puiiiokiq.anicat.backend.utils.service.S3Service;
import puiiiokiq.anicat.backend.episodes.models.Episode;
import puiiiokiq.anicat.backend.episodes.models.Audio;


import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import puiiiokiq.anicat.backend.episodes.Repository.EpisodeRepository;
import puiiiokiq.anicat.backend.episodes.Repository.AudioRepository;
import puiiiokiq.anicat.backend.anime.Repository.BannerRepository;
import puiiiokiq.anicat.backend.anime.Repository.CoverRepository;
import puiiiokiq.anicat.backend.anime.Repository.ScreenshotsRepository;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AnimeAdminController {

    private final AnimeService animeService;
    private final S3Service s3Service;
    private final AnimeRepository animeRepository;
    private final EpisodeRepository episodeRepository;
    private final AudioRepository audioRepository;
    private final BannerRepository bannerRepository;
    private final CoverRepository coverRepository;
    private final ScreenshotsRepository screenshotsRepository;



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
        if (files.size() > 8) {
            return ResponseEntity.badRequest().body("Максимум 7 скриншотов");
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

    @PostMapping("/upload-banner/{animeId}")
    public ResponseEntity<String> uploadBanner(
            @PathVariable Long animeId,
            @RequestParam("file") MultipartFile file
    ) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new RuntimeException("Аниме не найдено"));

        String originalName = file.getOriginalFilename(); // может быть кириллица, пробелы, любые символы

        // 1. Создание записи в БД с оригинальным именем
        Long bannerId = animeService.createBannerRecord(animeId, originalName);

        // 2. Путь в S3 — сохраняем ТОЛЬКО под ID.webp
        String filename = bannerId + ".webp";
        String path = "animes/" + animeId + "/banner/" + filename;

        try {
            // 3. Загрузка файла (без учёта расширения оригинала — как .webp)
            s3Service.uploadFile(path, file);

            return ResponseEntity.ok("✅ Баннер загружен как " + filename);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Ошибка загрузки баннера: " + e.getMessage());
        }
    }

    @PutMapping("/edit-info/{animeId}")
    public ResponseEntity<String> editAnimeInfo(
            @PathVariable Long animeId,
            @RequestBody AnimeInfoRequest request
    ) {
        Anime anime = animeRepository.findById(animeId).orElseThrow(() -> new RuntimeException("Аниме не найдено"));

        anime.setTitle(request.getTitle());
        anime.setAlttitle(request.getAlttitle());
        anime.setDescription(request.getDescription());
        anime.setGenres(request.getGenres());
        anime.setStatus(request.getStatus());
        anime.setType(request.getType());
        anime.setEpisodeAll(request.getEpisode_all());
        anime.setCurrentEpisode(request.getCurrent_episode());
        anime.setRating(request.getRating());
        anime.setYear(request.getYear());
        anime.setSeason(request.getSeason());
        anime.setMouthSeason(request.getMouth_season());
        anime.setStudio(request.getStudio());
        anime.setRealesedFor(request.getRealesed_for());
        anime.setAlias(request.getAlias());
        anime.setKodik(request.getKodik());

        animeRepository.save(anime);
        return ResponseEntity.ok("✅ Информация обновлена");
    }

    @PutMapping("/edit-cover/{animeId}")
    public ResponseEntity<String> editCover(
            @PathVariable Long animeId,
            @RequestParam("file") MultipartFile file
    ) {
        Anime anime = animeRepository.findById(animeId).orElseThrow();

        // Удаление старой обложки (если есть)
        if (anime.getCover() != null) {
            Long oldId = anime.getCover().getId();

            // 1. Убираем связь с cover
            anime.setCover(null);
            animeRepository.save(anime);

            // 2. Удаляем файл из S3
            String oldKey = "animes/" + animeId + "/cover/" + oldId + ".webp";
            s3Service.deleteFile(oldKey);

            // 3. Удаляем запись cover из базы
            coverRepository.deleteById(oldId);
        }

        // 4. Создаём новую обложку
        Cover cover = new Cover();
        cover.setAnime(anime);
        coverRepository.save(cover);

        // 5. Загружаем файл в S3
        String newKey = "animes/" + animeId + "/cover/" + cover.getId() + ".webp";
        s3Service.uploadFile(newKey, file);

        // 6. Привязываем обложку к аниме
        anime.setCover(cover);
        animeRepository.save(anime);

        return ResponseEntity.ok("✅ Обложка обновлена");
    }


    @PutMapping("/edit-banner/{animeId}")
    public ResponseEntity<String> editBanner(
            @PathVariable Long animeId,
            @RequestParam("file") MultipartFile file
    ) {
        Anime anime = animeRepository.findById(animeId).orElseThrow();

        // Удаление старого баннера (если есть)
        if (anime.getBanner() != null) {
            Long oldId = anime.getBanner().getId();

            // 1. Разрываем связь
            anime.setBanner(null);
            animeRepository.save(anime);

            // 2. Удаляем файл из S3
            String oldKey = "animes/" + animeId + "/banner/" + oldId + ".webp";
            s3Service.deleteFile(oldKey);

            // 3. Удаляем запись из базы
            bannerRepository.deleteById(oldId);
        }

        // 4. Создаём новый баннер
        Banner banner = new Banner();
        banner.setAnime(anime);
        bannerRepository.save(banner);

        // 5. Загружаем файл в S3
        String key = "animes/" + animeId + "/banner/" + banner.getId() + ".webp";
        s3Service.uploadFile(key, file);

        // 6. Привязываем баннер к аниме
        anime.setBanner(banner);
        animeRepository.save(anime);

        return ResponseEntity.ok("✅ Баннер обновлён");
    }


    @PutMapping("/edit-screenshots/{animeId}")
    public ResponseEntity<String> editScreenshots(
            @PathVariable Long animeId,
            @RequestParam("files") List<MultipartFile> newFiles,
            @RequestParam("keepIds") List<Long> keepIds // <-- Список ID, которые остаются
    ) {
        Anime anime = animeRepository.findById(animeId).orElseThrow();

        // Найдём старые скриншоты
        List<Screenshots> existingShots = screenshotsRepository.findByAnimeId(animeId);

        // Вычисляем, какие нужно удалить
        List<Screenshots> toRemove = existingShots.stream()
                .filter(s -> !keepIds.contains(s.getId()))
                .toList();

        for (Screenshots shot : toRemove) {
            String key = "animes/" + animeId + "/screenshots/" + shot.getId() + ".webp";
            s3Service.deleteFile(key);
        }
        screenshotsRepository.deleteAll(toRemove);

        // Загружаем новые
        for (MultipartFile file : newFiles) {
            Screenshots shot = new Screenshots();
            shot.setAnime(anime);
            screenshotsRepository.save(shot);

            String key = "animes/" + animeId + "/screenshots/" + shot.getId() + ".webp";
            s3Service.uploadFile(key, file);
        }

        return ResponseEntity.ok("✅ Скриншоты обновлены");
    }


    @DeleteMapping("/delete-cover/{animeId}")
    public ResponseEntity<String> deleteCover(@PathVariable Long animeId) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new RuntimeException("Аниме не найдено"));

        if (anime.getCover() != null) {
            Long coverId = anime.getCover().getId();

            // 1. Убираем связь
            anime.setCover(null);
            animeRepository.save(anime);

            // 2. Удаляем файл из S3
            String key = "animes/" + animeId + "/cover/" + coverId + ".webp";
            s3Service.deleteFile(key);

            // 3. Удаляем из БД
            coverRepository.deleteById(coverId);
        }

        return ResponseEntity.ok("✅ Обложка удалена");
    }

    @DeleteMapping("/delete-banner/{animeId}")
    public ResponseEntity<String> deleteBanner(@PathVariable Long animeId) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new RuntimeException("Аниме не найдено"));

        if (anime.getBanner() != null) {
            Long bannerId = anime.getBanner().getId();

            // 1. Убираем связь
            anime.setBanner(null);
            animeRepository.save(anime);

            // 2. Удаляем файл из S3
            String key = "animes/" + animeId + "/banner/" + bannerId + ".webp";
            s3Service.deleteFile(key);

            // 3. Удаляем из БД
            bannerRepository.deleteById(bannerId);
        }

        return ResponseEntity.ok("✅ Баннер удалён");
    }


}
