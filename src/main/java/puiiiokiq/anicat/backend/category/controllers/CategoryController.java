package puiiiokiq.anicat.backend.category.controllers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.category.AnimeCategory;
import puiiiokiq.anicat.backend.category.AnimeCategoryRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class CategoryController {

    private final AnimeCategoryRepository animeCategoryRepository;

    @PutMapping("/update-category/{categoryId}")
    public ResponseEntity<?> updateAnimeCategory(
            @PathVariable String categoryId,
            @RequestBody UpdateCategoryRequest request
    ) {
        Optional<AnimeCategory> optionalCategory = animeCategoryRepository.findById(categoryId);

        if (optionalCategory.isEmpty()) {
            return ResponseEntity.badRequest().body("Категория не найдена");
        }

        AnimeCategory category = optionalCategory.get();

        // Существующие ID
        List<String> currentIds = category.getAnimeIds();

        // Новые ID из запроса
        List<String> newIds = request.getAnimeIds().stream()
                .map(String::valueOf) // чтобы не было неоднозначности
                .toList();

        // Удаляем отсутствующие
        currentIds.removeIf(id -> !newIds.contains(id));

        // Добавляем новые
        for (String id : newIds) {
            if (!currentIds.contains(id)) {
                currentIds.add(id);
            }
        }

        animeCategoryRepository.save(category);

        return ResponseEntity.ok("✅ Категория успешно обновлена");
    }

    @Setter
    @Getter
    public static class UpdateCategoryRequest {
        private List<Long> animeIds;
    }
}
