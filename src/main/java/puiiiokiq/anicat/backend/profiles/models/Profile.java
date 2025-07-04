package puiiiokiq.anicat.backend.profiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    private String bio;
    private String avatarId;
    private String bannerId;
    private Boolean animePageBeta;   // ✅ добавляем сюда
    private Boolean profilePageBeta; // ✅ добавляем сюда
    private Boolean banned;
    private Boolean muted;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;
}
