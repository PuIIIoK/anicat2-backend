package puiiiokiq.anicat.backend.anime.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    @JsonBackReference
    private Anime anime;

}
