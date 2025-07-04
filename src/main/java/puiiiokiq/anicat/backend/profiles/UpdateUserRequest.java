package puiiiokiq.anicat.backend.profiles;

import lombok.Data;
import java.util.List;

@Data
public class UpdateUserRequest {
    private String username;
    private List<String> roles;
    private Boolean banned;
    private Boolean muted;

    private String nickname;
    private String bio;
}
