package puiiiokiq.anicat.backend.collections.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import puiiiokiq.anicat.backend.anime.models.Anime;
import puiiiokiq.anicat.backend.profiles.models.User;
import puiiiokiq.anicat.backend.utils.CollectionType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class AnimeCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "anime_id")  // Указываем имя колонки для связи
    private Anime anime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private CollectionType type;

    private LocalDateTime addedAt = LocalDateTime.now();

}
