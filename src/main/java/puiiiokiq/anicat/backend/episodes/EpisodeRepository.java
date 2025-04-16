package puiiiokiq.anicat.backend.episodes;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findByAnimeId(Long animeId);
}