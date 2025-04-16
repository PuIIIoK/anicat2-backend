package puiiiokiq.anicat.backend.anime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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