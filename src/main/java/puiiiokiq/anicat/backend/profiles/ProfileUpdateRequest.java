package puiiiokiq.anicat.backend.profiles;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String nickname;
    private String bio;
    private String avatarId;
    private String bannerId;
}
