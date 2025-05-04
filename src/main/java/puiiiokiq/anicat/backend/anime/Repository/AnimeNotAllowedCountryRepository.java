package puiiiokiq.anicat.backend.anime.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puiiiokiq.anicat.backend.anime.models.AnimeNotAllowedCountry;
import java.util.List;

public interface AnimeNotAllowedCountryRepository extends JpaRepository<AnimeNotAllowedCountry, Long> {
    List<AnimeNotAllowedCountry> findByAnimeId(Long animeId);
}
