package puiiiokiq.anicat.backend.profiles.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import puiiiokiq.anicat.backend.profiles.AuthService;
import puiiiokiq.anicat.backend.profiles.Repository.ProfileRepository;
import puiiiokiq.anicat.backend.profiles.Repository.UserRepository;
import puiiiokiq.anicat.backend.profiles.ReqResp.*;
import puiiiokiq.anicat.backend.profiles.models.Profile;
import puiiiokiq.anicat.backend.profiles.models.User;
import puiiiokiq.anicat.backend.profiles.models.UserProfile;


import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/check-auth")
    public ResponseEntity<String> checkAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Не авторизован");
        }

        return ResponseEntity.ok("Пользователь авторизован");
    }


    @GetMapping("/get-role")
    public ResponseEntity<List<String>> getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return userRepository.findByUsername(username)
                .map(user -> {
                    List<String> roleNames = user.getRoles().stream()
                            .map(Enum::name)
                            .toList();
                    return ResponseEntity.ok(roleNames);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of("UNKNOWN")));
    }


    @GetMapping("/get-profile")
    public UserProfile getMyProfile(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Profile profile = user.getProfile();

        return new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getRoles().stream().map(Enum::name).toArray(String[]::new),
                profile.getId(),
                profile.getNickname(),
                profile.getBio(),
                profile.getAvatarId(),
                profile.getBannerId(),
                Boolean.TRUE.equals(profile.getAnimePageBeta()),
                Boolean.TRUE.equals(profile.getProfilePageBeta())
        );

    }

    @GetMapping("/get-profile/{id}")
    public UserProfile getProfileById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Profile profile = user.getProfile();

        return new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getRoles().stream().map(Enum::name).toArray(String[]::new),
                profile.getId(),
                profile.getNickname(),
                profile.getBio(),
                profile.getAvatarId(),
                profile.getBannerId(),
                Boolean.TRUE.equals(profile.getAnimePageBeta()),
                Boolean.TRUE.equals(profile.getProfilePageBeta())
        );
    }

    @PostMapping("/set-profile")
    public ResponseEntity<String> setProfile(@RequestBody ProfileUpdateRequest request, Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Profile profile = user.getProfile();

        if (request.getNickname() != null) profile.setNickname(request.getNickname());
        if (request.getBio() != null) profile.setBio(request.getBio());

        if ("delete".equalsIgnoreCase(request.getAvatarId())) {
            profile.setAvatarId(null);
        } else if (request.getAvatarId() != null && !request.getAvatarId().isBlank()) {
            profile.setAvatarId(request.getAvatarId());
        }

        if ("delete".equalsIgnoreCase(request.getBannerId())) {
            profile.setBannerId(null);
        } else if (request.getBannerId() != null && !request.getBannerId().isBlank()) {
            profile.setBannerId(request.getBannerId());
        }

        userRepository.save(user); // Или profileRepository.save(profile)
        return ResponseEntity.ok("Профиль обновлён");
    }

    @PostMapping("/set-profile-id/{profileId}")
    public ResponseEntity<?> updateProfileById(@PathVariable Long profileId, @RequestBody ProfileUpdateRequest request) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Профиль не найден"));

        if (request.getNickname() != null) profile.setNickname(request.getNickname());
        if (request.getBio() != null) profile.setBio(request.getBio());

        if ("delete".equalsIgnoreCase(request.getAvatarId())) {
            profile.setAvatarId(null);
        } else if (request.getAvatarId() != null && !request.getAvatarId().isBlank()) {
            profile.setAvatarId(request.getAvatarId());
        }

        if ("delete".equalsIgnoreCase(request.getBannerId())) {
            profile.setBannerId(null);
        } else if (request.getBannerId() != null && !request.getBannerId().isBlank()) {
            profile.setBannerId(request.getBannerId());
        }

        profileRepository.save(profile);
        return ResponseEntity.ok("Профиль обновлён");
    }

    @PostMapping("/set-login")
    public ResponseEntity<?> updateLoginAndPassword(
            @RequestBody UpdateLoginRequest request,
            Authentication auth
    ) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return ResponseEntity.ok("Логин и/или пароль обновлены.");
    }


    @PostMapping("/set-login-id/{userId}")
    public ResponseEntity<?> updateLoginAndPasswordById(
            @PathVariable Long userId,
            @RequestBody UpdateLoginRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID %d не найден".formatted(userId)));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return ResponseEntity.ok("Логин и/или пароль обновлены по ID.");
    }

    @GetMapping("/check-admin-access")
    public ResponseEntity<?> checkAdminAccess(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Не авторизован");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    boolean isAdmin = user.getRoles().stream()
                            .anyMatch(role -> role.name().equals("ADMIN"));

                    if (isAdmin) {
                        URI redirectUri = URI.create(frontendUrl + "/admin_panel");
                        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нет доступа");
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не найден"));
    }

    @GetMapping("/user-info")
    public ResponseEntity<? extends Map<String, ? extends Object>> getUserInfo(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Не авторизован")
            );
        }

        String username = auth.getName();

        return userRepository.findByUsername(username)
                .map(user -> {
                    // Только если есть токен в запросе — возвращаем логин и роль
                    String token = extractTokenFromRequest(request);
                    if (token.equals("N/A")) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                Map.of("error", "Токен не найден")
                        );
                    }

                    List<String> roles = user.getRoles().stream()
                            .map(Enum::name)
                            .toList();

                    return ResponseEntity.ok(Map.of(
                            "username", user.getUsername(),
                            "roles", roles
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("error", "Пользователь не найден")
                ));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // удалить "Bearer "
        }
        return "N/A";
    }
}