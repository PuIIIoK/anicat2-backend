package puiiiokiq.anicat.backend.shop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private String orderId;

    private String username;

    private String ip;

    private String status;

    private int amount; // в копейках

    private LocalDateTime createdAt;

    private boolean credited = false;

    public Payment() {
        // обязательный конструктор для JPA
    }

    public Payment(String orderId, String username, String ip, String status, int amount, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.username = username;
        this.ip = ip;
        this.status = status;
        this.amount = amount;
        this.createdAt = createdAt;
    }
    // === Setters ===

}
