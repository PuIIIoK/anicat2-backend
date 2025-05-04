package puiiiokiq.anicat.backend.anime.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import puiiiokiq.anicat.backend.anime.Repository.CoverRepository;
import puiiiokiq.anicat.backend.anime.models.Cover;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoverService {
    private final CoverRepository coverRepository;

    public List<Cover> getAll() {
        return coverRepository.findAll();
    }

    public Cover getById(Long id) {
        return coverRepository.findById(id).orElse(null);
    }
}