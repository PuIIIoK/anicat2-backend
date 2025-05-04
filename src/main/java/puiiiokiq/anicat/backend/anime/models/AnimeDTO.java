package puiiiokiq.anicat.backend.anime.models;

import lombok.Data;

import java.util.List;

@Data
public class AnimeDTO {
    private Long id;
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
    private String kodik;
    private String alias;
    private String realesed_for;
    private String imageUrl;
    private List<String> allowedCountries;

    public AnimeDTO(Anime anime, String imageUrl) {
        this.id = anime.getId();
        this.title = anime.getTitle();
        this.alttitle = anime.getAlttitle();
        this.description = anime.getDescription();
        this.genres = anime.getGenres();
        this.status = anime.getStatus();
        this.type = anime.getType();
        this.episode_all = anime.getEpisodeAll();
        this.current_episode = anime.getCurrentEpisode();
        this.rating = anime.getRating();
        this.year = anime.getYear();
        this.season = anime.getSeason();
        this.mouth_season = anime.getMouthSeason();
        this.studio = anime.getStudio();
        this.realesed_for = anime.getRealesedFor();
        this.imageUrl = imageUrl;
        this.kodik = anime.getKodik();
        this.alias = anime.getAlias();
        this.allowedCountries = anime.getAllowedCountries();
    }
}
