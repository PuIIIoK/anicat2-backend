package puiiiokiq.anicat.backend.profiles.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import puiiiokiq.anicat.backend.profiles.models.Profile;
import puiiiokiq.anicat.backend.profiles.models.User;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("SELECT p FROM Profile p WHERE p.user.id = :userId")
    Optional<Profile> findByUserId(Long userId);
    Optional<Profile> findByUser(User user);
    Optional<Profile> findByNickname(String nickname);
}
