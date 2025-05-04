package puiiiokiq.anicat.backend.anime;

import lombok.Data;

@Data
public class AnimeInfoRequest {
    private String title;
    private String alttitle;
    private String description;
    private String genres;
    private String status;
    private String type;
    private String episode_all;
    private String current_episode;
    private String rating;
    private String year;
    private String season;
    private String mouth_season;
    private String studio;
    private String realesed_for;
    private String alias;
    private String kodik;
}
