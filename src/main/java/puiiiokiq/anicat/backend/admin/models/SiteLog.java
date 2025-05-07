package puiiiokiq.anicat.backend.admin.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "site_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action; // Например: "Добавлено аниме", "Изменена роль"
    private String target; // Например: "Аниме: Naruto", "Пользователь: admin"
    private String performedBy; // Кто выполнил
    private LocalDateTime timestamp;
}
