package puiiiokiq.anicat.backend.episodes.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puiiiokiq.anicat.backend.episodes.models.Audio;

import java.util.List;
import java.util.Optional;

public interface AudioRepository extends JpaRepository<Audio, Long> {
    List<Audio> findByAnimeId(Long animeId);
    Optional<Audio> findByAnimeIdAndName(Long animeId, String name);
    Optional<Audio> findByEpisodeIdAndName(Long episodeId, String name);
    List<Audio> findByEpisodeId(Long episodeId);
    void deleteByAnimeId(Long animeId);
}
