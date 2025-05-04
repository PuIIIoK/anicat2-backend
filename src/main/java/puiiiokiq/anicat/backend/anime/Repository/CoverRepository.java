package puiiiokiq.anicat.backend.anime.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puiiiokiq.anicat.backend.anime.models.Cover;

import java.util.List;

public interface CoverRepository extends JpaRepository<Cover, Long> {
    List<Cover> findByAnimeId(Long animeId);
    void deleteByAnimeId(Long animeId);
    boolean existsByAnimeId(Long animeId);
}
