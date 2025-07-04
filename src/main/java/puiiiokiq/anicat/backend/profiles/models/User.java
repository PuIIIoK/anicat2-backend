package puiiiokiq.anicat.backend.profiles.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import puiiiokiq.anicat.backend.utils.Role;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Column(name = "roles")
    private String rolesString;

    @Column(name = "balance")
    private Double balance;

    @Transient
    public List<Role> getRoles() {
        if (rolesString == null || rolesString.isBlank()) return List.of();
        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .map(Role::valueOf)
                .collect(Collectors.toList());
    }

    public void setRoles(List<Role> roles) {
        this.rolesString = roles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;
}
