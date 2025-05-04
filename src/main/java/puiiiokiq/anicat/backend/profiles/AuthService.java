package puiiiokiq.anicat.backend.profiles;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import puiiiokiq.anicat.backend.profiles.Repository.UserRepository;
import puiiiokiq.anicat.backend.profiles.ReqResp.AuthResponse;
import puiiiokiq.anicat.backend.profiles.ReqResp.LoginRequest;
import puiiiokiq.anicat.backend.profiles.ReqResp.RegisterRequest;
import puiiiokiq.anicat.backend.profiles.models.Profile;
import puiiiokiq.anicat.backend.profiles.models.User;
import puiiiokiq.anicat.backend.utils.Role;
import puiiiokiq.anicat.backend.utils.service.JwtService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Пользователь уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(List.of(Role.USER)); // ✅ храним как строку, но в коде как список

        Profile profile = new Profile();
        profile.setUser(user);
        user.setProfile(profile);

        userRepo.save(user);

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRoles().stream().map(Enum::name).findFirst().orElse("USER"),
                user.getId()
        );
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверный пароль");
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRoles().stream().map(Enum::name).findFirst().orElse("USER"),
                user.getId()
        );
        return new AuthResponse(token);
    }
}
