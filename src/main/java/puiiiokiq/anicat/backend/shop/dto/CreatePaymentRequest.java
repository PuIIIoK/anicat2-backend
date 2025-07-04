package puiiiokiq.anicat.backend.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreatePaymentRequest {
    private String username;
    private int rubles;

}
