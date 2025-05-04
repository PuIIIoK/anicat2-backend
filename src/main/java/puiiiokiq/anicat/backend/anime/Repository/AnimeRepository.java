package puiiiokiq.anicat.backend.anime.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import puiiiokiq.anicat.backend.anime.models.Anime;

import java.util.List;

public interface AnimeRepository extends JpaRepository<Anime, Long> {

}
