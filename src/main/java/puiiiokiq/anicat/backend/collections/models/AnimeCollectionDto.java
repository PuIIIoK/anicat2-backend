package puiiiokiq.anicat.backend.collections.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import puiiiokiq.anicat.backend.anime.models.AnimeDTO;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimeCollectionDto {
    private Long collectionId;
    private String collectionType;
    private LocalDateTime addedAt;
    private AnimeDTO anime;
}
