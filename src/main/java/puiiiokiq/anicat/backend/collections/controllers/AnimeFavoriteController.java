package puiiiokiq.anicat.backend.collections.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.collections.AnimeCollectionService;
import puiiiokiq.anicat.backend.utils.CollectionType;
import puiiiokiq.anicat.backend.utils.service.JwtService;

@RestController
@RequestMapping("/api/collection/favorite")
public class AnimeFavoriteController {

    @Autowired
    private AnimeCollectionService collectionService;

    @Autowired
    private JwtService jwtService;

    // ✅ Получение userId из JWT токена
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            return jwtService.extractUserId(jwt);
        }
        throw new RuntimeException("Токен не найден или некорректный");
    }

    // ✅ 1. Добавить в избранное
    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(
            @RequestParam Long animeId,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromToken(request);
        collectionService.setAnimeStatus(userId, animeId, CollectionType.FAVORITE);
        return ResponseEntity.ok("Аниме добавлено в избранное");
    }

    // ✅ 2. Удалить из избранного
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFavorite(
            @RequestParam Long animeId,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromToken(request);
        collectionService.removeFromFavorite(userId, animeId);
        return ResponseEntity.ok("Аниме удалено из избранного");
    }
}
