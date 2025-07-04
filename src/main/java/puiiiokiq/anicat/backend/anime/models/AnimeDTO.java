package puiiiokiq.anicat.backend.anime.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private String zametka;
    private List<String> allowedCountries;


    public static AnimeDTO from(Anime anime, String imageUrl) {
        return new AnimeDTO(
                anime.getId(),
                anime.getTitle(),
                anime.getAlttitle(),
                anime.getDescription(),
                anime.getGenres(),
                anime.getStatus(),
                anime.getType(),
                anime.getEpisodeAll(),
                anime.getCurrentEpisode(),
                anime.getRating(),
                anime.getYear(),
                anime.getSeason(),
                anime.getMouthSeason(),
                anime.getStudio(),
                anime.getKodik(),
                anime.getAlias(),
                anime.getRealesedFor(),
                imageUrl,
                anime.getZametka(),
                anime.getAllowedCountries()
        );
    }
}
