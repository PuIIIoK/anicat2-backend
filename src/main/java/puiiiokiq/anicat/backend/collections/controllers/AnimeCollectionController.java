package puiiiokiq.anicat.backend.collections.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.anime.models.Anime;
import puiiiokiq.anicat.backend.collections.AnimeCollectionService;
import puiiiokiq.anicat.backend.collections.models.AnimeCollection;
import puiiiokiq.anicat.backend.utils.CollectionType;
import puiiiokiq.anicat.backend.utils.service.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/collection")
public class AnimeCollectionController {

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

    // ✅ 1. Добавить в коллекцию
    @PostMapping("/set")
    public ResponseEntity<?> setCollectionStatus(
            @RequestParam Long animeId,
            @RequestParam CollectionType type,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromToken(request);
        collectionService.setAnimeStatus(userId, animeId, type);
        return ResponseEntity.ok("Коллекция обновлена");
    }

    // ✅ 2. Удалить из коллекции
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCollection(
            @RequestParam Long animeId,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromToken(request);
        collectionService.removeFromCollection(userId, animeId);
        return ResponseEntity.ok("Аниме удалено из коллекции");
    }

    // ✅ 3. Получить коллекции по токену (авторизованного пользователя)
    @GetMapping("/my")
    public ResponseEntity<?> getMyCollections(
            @RequestParam(required = false) CollectionType type,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromToken(request);
        if (type != null) {
            return ResponseEntity.ok(collectionService.getUserCollectionsByTypeDto(userId, type));
        } else {
            return ResponseEntity.ok(collectionService.getUserCollectionsDto(userId));
        }
    }
}
