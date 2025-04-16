package puiiiokiq.anicat.backend.anime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Screenshots {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // название скриншота, например: screenshot1.png

    @ManyToOne
    @JoinColumn(name = "anime_id")
    @JsonBackReference
    private Anime anime; // связь с аниме
}
