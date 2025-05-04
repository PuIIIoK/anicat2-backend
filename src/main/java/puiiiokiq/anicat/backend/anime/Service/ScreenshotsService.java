package puiiiokiq.anicat.backend.anime.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import puiiiokiq.anicat.backend.anime.Repository.ScreenshotsRepository;
import puiiiokiq.anicat.backend.anime.models.Screenshots;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenshotsService {
    private final ScreenshotsRepository screenshotsRepository;

    public List<Screenshots> getAll() {
        return screenshotsRepository.findAll();
    }

    public Screenshots getById(Long id) {
        return screenshotsRepository.findById(id).orElse(null);
    }
}