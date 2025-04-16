package puiiiokiq.anicat.backend.profiles;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository; // ← добавь это
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
    public String checkAuth() {
        return "Пользователь авторизован";
    }

    @GetMapping("/get-role")
    public String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(Object::toString)
                .reduce((r1, r2) -> r1 + "," + r2)
                .orElse("UNKNOWN");
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
                profile.getBannerId()
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
                profile.getBannerId()
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


}