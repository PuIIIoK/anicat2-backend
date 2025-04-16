package puiiiokiq.anicat.backend.anime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Cover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // название обложки, например: cover1.webp

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "anime_id")
    private Anime anime; // связь с аниме
}
