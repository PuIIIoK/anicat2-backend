package puiiiokiq.anicat.backend.profiles;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String bio;
    private String avatarId;
    private String bannerId;
}
