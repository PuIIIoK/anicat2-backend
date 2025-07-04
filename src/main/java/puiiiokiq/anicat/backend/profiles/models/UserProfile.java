package puiiiokiq.anicat.backend.profiles.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    private Long userId;
    private String username;
    private String[] roles;

    private Long profileId;
    private String nickname;
    private String bio;
    private String avatarId;
    private String bannerId;
    private Boolean banned;
    private Boolean muted;
}
