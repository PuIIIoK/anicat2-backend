package puiiiokiq.anicat.backend.profiles.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.profiles.UpdateUserRequest;
import puiiiokiq.anicat.backend.profiles.models.Profile;
import puiiiokiq.anicat.backend.profiles.models.User;
import puiiiokiq.anicat.backend.profiles.Repository.UserRepository;
import puiiiokiq.anicat.backend.profiles.models.UserProfile;
import puiiiokiq.anicat.backend.utils.Role;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping("/list")
    public List<UserProfile> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            Profile profile = user.getProfile();
            return new UserProfile(
                    user.getId(),
                    user.getUsername(),
                    user.getRoles().stream().map(Enum::name).toArray(String[]::new),

                    profile != null ? profile.getId() : null,
                    profile != null ? profile.getNickname() : "",
                    profile != null ? profile.getBio() : "",
                    profile != null ? profile.getAvatarId() : null,
                    profile != null ? profile.getBannerId() : null,

                    user.getBanned(),
                    user.getMuted()
            );
        }).toList();
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(
            @RequestParam("by") String by,
            @RequestParam("value") String value,
            @RequestBody UpdateUserRequest request
    ) {
        return getUserBy(by, value).map(user -> {
            boolean updated = false;

            if (request.getUsername() != null && !request.getUsername().isBlank()
                    && !request.getUsername().equals(user.getUsername())) {
                user.setUsername(request.getUsername());
                updated = true;
            }

            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                List<Role> newRoles = request.getRoles().stream()
                        .map(role -> {
                            try {
                                return Role.valueOf(role.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                throw new RuntimeException("Неизвестная роль: " + role);
                            }
                        })
                        .toList();

                if (!newRoles.equals(user.getRoles())) {
                    user.setRoles(newRoles);
                    updated = true;
                }
            }

            if (request.getBanned() != null && !request.getBanned().equals(user.getBanned())) {
                user.setBanned(request.getBanned());
                updated = true;
            }

            if (request.getMuted() != null && !request.getMuted().equals(user.getMuted())) {
                user.setMuted(request.getMuted());
                updated = true;
            }

            Profile profile = user.getProfile();
            if (profile != null) {
                if (request.getNickname() != null && !request.getNickname().equals(profile.getNickname())) {
                    profile.setNickname(request.getNickname());
                    updated = true;
                }

                if (request.getBio() != null && !request.getBio().equals(profile.getBio())) {
                    profile.setBio(request.getBio());
                    updated = true;
                }
            }

            if (!updated) return ResponseEntity.ok("Ничего не изменено");

            userRepository.save(user);
            return ResponseEntity.ok("Пользователь обновлён");

        }).orElse(ResponseEntity.status(404).body("Пользователь не найден"));
    }

    private java.util.Optional<User> getUserBy(String by, String value) {
        return switch (by.toLowerCase()) {
            case "id" -> {
                try {
                    yield userRepository.findById(Long.parseLong(value));
                } catch (NumberFormatException e) {
                    yield java.util.Optional.empty();
                }
            }
            case "username" -> userRepository.findByUsername(value);
            default -> java.util.Optional.empty();
        };
    }

}
