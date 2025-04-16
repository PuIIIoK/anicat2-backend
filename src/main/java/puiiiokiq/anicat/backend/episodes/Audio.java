
package puiiiokiq.anicat.backend.episodes;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import puiiiokiq.anicat.backend.anime.Anime;

import java.util.List;

@Getter
@Setter
@Entity
public class Audio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "anime_id")
    @JsonBackReference
    private Anime anime;

    @ManyToOne
    @JoinColumn(name = "episode_id")
    @JsonBackReference(value = "episode-audio")
    private Episode episode;
}
