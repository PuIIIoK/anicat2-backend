package puiiiokiq.anicat.backend.category;

import org.springframework.stereotype.Service;
import puiiiokiq.anicat.backend.category.AnimeCategory;
import puiiiokiq.anicat.backend.category.AnimeCategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AnimeCategoryService {

    private final AnimeCategoryRepository repository;

    public AnimeCategoryService(AnimeCategoryRepository repository) {
        this.repository = repository;
    }

    public List<AnimeCategory> getAllCategories() {
        return repository.findAll();
    }

    public Optional<AnimeCategory> getCategoryById(String categoryId) {
        return repository.findById(categoryId);
    }

}
