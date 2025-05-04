package puiiiokiq.anicat.backend.episodes.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puiiiokiq.anicat.backend.episodes.models.Episode;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findByAnimeId(Long animeId);

    void deleteByAnimeId(Long animeId);

    boolean existsByAnimeId(Long animeId);
}
