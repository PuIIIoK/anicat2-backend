package puiiiokiq.anicat.backend.anime.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puiiiokiq.anicat.backend.anime.models.Banner;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByAnimeId(Long animeId);
}
