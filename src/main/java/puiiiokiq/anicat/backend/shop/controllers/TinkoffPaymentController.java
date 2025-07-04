package puiiiokiq.anicat.backend.shop.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.shop.TinkoffPaymentService;
import puiiiokiq.anicat.backend.shop.dto.CreatePaymentRequest;



@RestController
@RequestMapping("/api/payment")
public class TinkoffPaymentController {

    private final TinkoffPaymentService tinkoffPaymentService;

    public TinkoffPaymentController(TinkoffPaymentService tinkoffPaymentService) {
        this.tinkoffPaymentService = tinkoffPaymentService;
    }

    @PostMapping("/live")
    public ResponseEntity<?> create(@RequestBody CreatePaymentRequest request) {
        return tinkoffPaymentService.createPayment(request.getUsername(), request.getRubles());
    }
}

