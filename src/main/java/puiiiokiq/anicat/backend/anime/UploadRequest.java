package puiiiokiq.anicat.backend.anime;

import lombok.Data;

@Data
public class UploadRequest {
    private Long animeId;
    private Long episodeId;
    private String audioName;
}
