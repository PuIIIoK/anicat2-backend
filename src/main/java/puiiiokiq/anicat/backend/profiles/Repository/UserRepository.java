package puiiiokiq.anicat.backend.profiles.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puiiiokiq.anicat.backend.profiles.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
}