package puiiiokiq.anicat.backend.anime;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import puiiiokiq.anicat.backend.episodes.Audio;
import puiiiokiq.anicat.backend.episodes.AudioRepository;
import puiiiokiq.anicat.backend.episodes.Episode;
import puiiiokiq.anicat.backend.episodes.EpisodeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;
    private final CoverRepository coverRepository;
    private final ScreenshotsRepository screenshotsRepository;
    private final EpisodeRepository episodeRepository;
    private final AudioRepository audioRepository;

    // Получить одно аниме по ID
    public Anime getAnimeById(Long id) {
        return animeRepository.findById(id).orElse(null);
    }

    // Получить список всех аниме
    public List<Anime> getAllAnime() {
        return animeRepository.findAll();
    }

    // 1. Создание нового пустого аниме и возврат его ID
    public Long createAnimeAndReturnId() {
        Anime anime = new Anime();
        anime.setTitle("Новое аниме");
        anime = animeRepository.save(anime);
        return anime.getId();
    }

    // 2. Удаление аниме и всех связанных данных
    @Transactional
    public void deleteAnimeWithRelations(Long animeId) {
        episodeRepository.deleteByAnimeId(animeId);
        audioRepository.deleteByAnimeId(animeId);
        screenshotsRepository.deleteByAnimeId(animeId);
        coverRepository.deleteByAnimeId(animeId);
        animeRepository.deleteById(animeId);
    }

    // 3. Создание записи обложки
    public Long createCoverRecord(Long animeId) {
        Anime anime = animeRepository.findById(animeId).orElseThrow();
        Cover cover = new Cover();
        cover.setAnime(anime);
        cover.setName(""); // можно позже задать имя файла
        cover = coverRepository.save(cover);
        return cover.getId();
    }

    // 4. Сохранение полной информации об аниме
    public void saveAnimeInfo(Long animeId, AnimeInfoRequest request) {
        Anime anime = animeRepository.findById(animeId).orElseThrow();
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

        animeRepository.save(anime);
    }

    // 5. Создание записи скриншота
    public Long createScreenshotRecord(Long animeId) {
        Anime anime = animeRepository.findById(animeId).orElseThrow();
        Screenshots shot = new Screenshots();
        shot.setAnime(anime);
        shot.setName(""); // Название файла можно будет задать позже
        shot = screenshotsRepository.save(shot);
        return shot.getId();
    }


    public Long getExistingEpisodeId(Long animeId) {
        // Здесь можешь настроить свою логику получения последнего эпизода, если нужно
        List<Episode> episodes = episodeRepository.findByAnimeId(animeId);
        return episodes.isEmpty() ? null : episodes.get(episodes.size() - 1).getId(); // последний добавленный
    }

    public boolean checkAnimeUpload(Long animeId) {
        Anime anime = animeRepository.findById(animeId).orElse(null);
        if (anime == null) return false;

        // Проверка только основной информации и обложки — игнорируем episode, audio и alias
        return anime.getTitle() != null && !anime.getTitle().isBlank()
                && anime.getDescription() != null && !anime.getDescription().isBlank()
                && anime.getGenres() != null && !anime.getGenres().isBlank()
                && anime.getStatus() != null && !anime.getStatus().isBlank()
                && anime.getType() != null && !anime.getType().isBlank()
                && anime.getEpisodeAll() != null && !anime.getEpisodeAll().isBlank()
                && anime.getCurrentEpisode() != null && !anime.getCurrentEpisode().isBlank()
                && anime.getYear() != null && !anime.getYear().isBlank()
                && anime.getSeason() != null && !anime.getSeason().isBlank()
                && anime.getMouthSeason() != null && !anime.getMouthSeason().isBlank()
                && anime.getStudio() != null && !anime.getStudio().isBlank()
                && anime.getRealesedFor() != null && !anime.getRealesedFor().isBlank()
                && anime.getCover() != null;
    }

    // 8. Добавление название эп в бд

    public Long createEpisodeRecord(Long animeId, String title) {
        Anime anime = animeRepository.findById(animeId).orElseThrow();
        Episode episode = new Episode();
        episode.setAnime(anime);
        episode.setTitle(title);
        episodeRepository.save(episode);
        return episode.getId();
    }
}
