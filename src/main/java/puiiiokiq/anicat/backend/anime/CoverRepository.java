package puiiiokiq.anicat.backend.anime;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoverRepository extends JpaRepository<Cover, Long> {
    List<Cover> findByAnimeId(Long animeId);
    void deleteByAnimeId(Long animeId);
    boolean existsByAnimeId(Long animeId);
}
