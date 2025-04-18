package puiiiokiq.anicat.backend.episodes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnimeEpisodeService {

    private final EpisodeRepository episodeRepository;
    private final AudioRepository audioRepository;

    public List<Episode> getEpisodesByAnimeId(Long animeId) {
        return episodeRepository.findByAnimeId(animeId);
    }

    public List<Audio> getAudiosByAnimeId(Long animeId) {
        return audioRepository.findByAnimeId(animeId);
    }

    public Optional<Audio> getAudio(Long episodeId, String name) {
        return audioRepository.findByEpisodeIdAndName(episodeId, name);
    }
}