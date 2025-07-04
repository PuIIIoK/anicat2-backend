package puiiiokiq.anicat.backend.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puiiiokiq.anicat.backend.shop.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}
