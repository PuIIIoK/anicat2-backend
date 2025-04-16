package puiiiokiq.anicat.backend.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
public class AnimeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private int position;

    @ElementCollection
    private List<String> animeIds;

    // Геттеры и сеттеры

}
