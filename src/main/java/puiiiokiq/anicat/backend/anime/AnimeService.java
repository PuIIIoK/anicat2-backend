package puiiiokiq.anicat.backend.anime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimeService {

    private final AnimeRepository animeRepository;

    @Autowired
    public AnimeService(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    // Метод для получения аниме по ID
    public Anime getAnimeById(Long id) {
        return animeRepository.findById(id).orElse(null); // Если аниме не найдено, возвращаем null
    }

    // Метод для получения всех аниме
    public List<Anime> getAllAnime() {
        return animeRepository.findAll(); // Возвращаем все аниме из базы данных
    }
}
