package puiiiokiq.anicat.backend.category;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/anime/category")
public class AnimeCategoryController {

    private final AnimeCategoryService service;

    public AnimeCategoryController(AnimeCategoryService service) {
        this.service = service;
    }

    @GetMapping("/get-category")
    public Map<String, Object> getCategories() {
        List<AnimeCategory> categories = service.getAllCategories();
        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        return response;
    }

    @GetMapping("/get-category/{categoryId}")
    public ResponseEntity<?> getCategoryById(@PathVariable String categoryId) {
        Optional<AnimeCategory> categoryOpt = service.getCategoryById(categoryId);

        if (categoryOpt.isPresent()) {
            return ResponseEntity.ok(categoryOpt.get());
        } else {
            return ResponseEntity.status(404).body("Категория с ID " + categoryId + " не найдена.");
        }
    }
}
