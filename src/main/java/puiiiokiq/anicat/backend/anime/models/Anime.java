package puiiiokiq.anicat.backend.anime.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import puiiiokiq.anicat.backend.collections.models.AnimeCollection;
import puiiiokiq.anicat.backend.episodes.models.Episode;

import java.util.List;

@Getter
@Setter
@Entity
public class Anime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String alttitle;

    @Column(length = 4000)
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
    private String zametka;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "anime_not_allowed_countries", joinColumns = @JoinColumn(name = "anime_id"))
    @Column(name = "country")
    private List<String> allowedCountries;

    @OneToMany(mappedBy = "anime", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Episode> episodes;

    @OneToMany(mappedBy = "anime", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Screenshots> screenshots;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_id")
    @JsonIgnore
    private Cover cover;

    @OneToOne(mappedBy = "anime", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Banner banner;

    // URL обложки
    @Transient
    public String getImageUrl() {
        if (cover != null && cover.getId() != null) {
            return "/api/stream/anime/" + id + "/cover/" + cover.getId();
        } else {
            return "/anime-cover-default.jpg";
        }
    }

    // URL баннера
    @Transient
    public String getBannerUrl() {
        if (banner != null && banner.getId() != null) {
            return "/api/stream/anime/" + id + "/banner/" + banner.getId();
        } else {
            return "/anime-banner-default.jpg";
        }
    }

    // геттеры/сеттеры ручные
    public String getEpisodeAll() {
        return episode_all;
    }

    public void setEpisodeAll(String episode_all) {
        this.episode_all = episode_all;
    }

    public String getCurrentEpisode() {
        return current_episode;
    }

    public void setCurrentEpisode(String current_episode) {
        this.current_episode = current_episode;
    }

    public String getMouthSeason() {
        return mouth_season;
    }

    public void setMouthSeason(String mouth_season) {
        this.mouth_season = mouth_season;
    }

    public String getRealesedFor() {
        return realesed_for;
    }

    public void setRealesedFor(String realesed_for) {
        this.realesed_for = realesed_for;
    }
}
