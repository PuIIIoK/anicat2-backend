package puiiiokiq.anicat.backend.collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import puiiiokiq.anicat.backend.anime.Repository.AnimeRepository;
import puiiiokiq.anicat.backend.anime.models.Anime;
import puiiiokiq.anicat.backend.anime.models.AnimeDTO;
import puiiiokiq.anicat.backend.collections.models.AnimeCollection;
import puiiiokiq.anicat.backend.collections.models.AnimeCollectionDto;
import puiiiokiq.anicat.backend.collections.repository.AnimeCollectionRepository;
import puiiiokiq.anicat.backend.profiles.Repository.UserRepository;
import puiiiokiq.anicat.backend.profiles.models.User;
import puiiiokiq.anicat.backend.utils.CollectionType;
import puiiiokiq.anicat.backend.utils.service.S3Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnimeCollectionService {
    @Autowired private AnimeCollectionRepository repo;
    @Autowired private UserRepository userRepo;
    @Autowired private AnimeRepository animeRepo;
    @Autowired private S3Service s3Service;
    @Autowired private UserRepository userRepository;
    @Autowired private AnimeCollectionRepository animeCollectionRepository;
    @Autowired private AnimeRepository animeRepository;

    public void setAnimeStatus(Long userId, Long animeId, CollectionType type) {
        User user = userRepo.findById(userId).orElseThrow();
        Anime anime = animeRepo.findById(animeId).orElseThrow();

        Optional<AnimeCollection> existing = repo.findByUserIdAndAnimeIdAndType(userId, animeId, type);

        if (type == CollectionType.NONE) {
            existing.ifPresent(repo::delete);
            return;
        }

        if (existing.isPresent()) {
            AnimeCollection record = existing.get();
            record.setType(type);
            repo.save(record);
        } else {
            AnimeCollection record = new AnimeCollection();
            record.setUser(user);
            record.setAnime(anime);
            record.setType(type);
            repo.save(record);
        }
    }

    public void removeFromCollection(Long userId, Long animeId) {
        List<AnimeCollection> allTypes = repo.findAllByUserIdAndAnimeId(userId, animeId);
        repo.deleteAll(allTypes);
    }

    public void removeFromFavorite(Long userId, Long animeId) {
        Optional<AnimeCollection> favorite = repo.findByUserIdAndAnimeIdAndType(userId, animeId, CollectionType.FAVORITE);
        favorite.ifPresent(repo::delete);
    }

    public List<AnimeCollection> getUserCollections(Long userId) {
        return repo.findByUserId(userId);
    }

    public List<AnimeCollection> getUserCollectionsByType(Long userId, CollectionType type) {
        return repo.findByUserIdAndType(userId, type);
    }

    public List<AnimeCollectionDto> getUserCollectionsDto(Long userId) {
        return repo.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AnimeCollectionDto> getUserCollectionsByTypeDto(Long userId, CollectionType type) {
        return repo.findByUserIdAndType(userId, type).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AnimeCollectionDto mapToDto(AnimeCollection entity) {
        Anime anime = entity.getAnime();
        String imageUrl = "/anime-cover-default.jpg";

        if (anime.getCover() != null && anime.getCover().getId() != null) {
            imageUrl = s3Service.generatePresignedCoverUrl(anime.getId(), anime.getCover().getId());
        }

        AnimeDTO animeDto = AnimeDTO.from(anime, imageUrl);

        return new AnimeCollectionDto(
                entity.getId(),
                entity.getType().name(),
                entity.getAddedAt(),
                animeDto
        );
    }
}

