package puiiiokiq.anicat.backend.episodes;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AudioRepository extends JpaRepository<Audio, Long> {
    List<Audio> findByAnimeId(Long animeId);
    Optional<Audio> findByAnimeIdAndName(Long animeId, String name);
    Optional<Audio> findByEpisodeIdAndName(Long episodeId, String name);
    List<Audio> findByEpisodeId(Long episodeId);
    void deleteByAnimeId(Long animeId);
}
