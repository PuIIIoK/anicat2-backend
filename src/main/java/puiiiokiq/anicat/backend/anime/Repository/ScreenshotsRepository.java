package puiiiokiq.anicat.backend.anime.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puiiiokiq.anicat.backend.anime.models.Screenshots;

import java.util.List;

public interface ScreenshotsRepository extends JpaRepository<Screenshots, Long> {
    List<Screenshots> findByAnimeId(Long animeId);
    void deleteByAnimeId(Long animeId);
    boolean existsByAnimeId(Long animeId);
}
