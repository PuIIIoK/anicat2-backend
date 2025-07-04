package puiiiokiq.anicat.backend.collections.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puiiiokiq.anicat.backend.collections.models.AnimeCollection;
import puiiiokiq.anicat.backend.utils.CollectionType;

import java.util.List;
import java.util.Optional;

public interface AnimeCollectionRepository extends JpaRepository<AnimeCollection, Long> {
    Optional<AnimeCollection> findByUserIdAndAnimeId(Long userId, Long animeId);
    Optional<AnimeCollection> findByUserIdAndAnimeIdAndType(Long userId, Long animeId, CollectionType type);
    List<AnimeCollection> findByUserId(Long userId);
    List<AnimeCollection> findByUserIdAndType(Long userId, CollectionType type);
    List<AnimeCollection> findAllByUserIdAndAnimeId(Long userId, Long animeId);
}
