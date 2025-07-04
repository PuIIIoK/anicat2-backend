package puiiiokiq.anicat.backend.shop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TinkoffPaymentService {

    private static final Logger log = LoggerFactory.getLogger(TinkoffPaymentService.class);

    private static final String INIT_URL = "https://securepay.tinkoff.ru/v2/Init";
    private static final String TERMINAL_KEY = "1749416625350"; // боевой или тестовый
    private static final String PASSWORD = "ZO4eKtWb*LOFb34h"; // боевой или тестовый

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<?> createPayment(String username, int rubles) {
        long start = System.currentTimeMillis();

        int amount = rubles * 100;
        String orderId = "order-" + UUID.randomUUID();
        String redirectDueDate = LocalDateTime.now().plusDays(1)
                .atZone(TimeZone.getTimeZone("Europe/Moscow").toZoneId())
                .toOffsetDateTime().toString();

        log.info("🚀 Создание платежа для пользователя: {}", username);
        log.info("💸 Сумма: {} руб. ({} коп.) | OrderID: {}", rubles, amount, orderId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("TerminalKey", TERMINAL_KEY);
        payload.put("Amount", amount);
        payload.put("OrderId", orderId);
        payload.put("Description", "Пополнение баланса AniCat");
        payload.put("CustomerKey", username);
        payload.put("PayType", "O");
        payload.put("Language", "ru");
        payload.put("NotificationURL", "https://anicat.fun/api/payment/callback");
        payload.put("SuccessURL", "https://anicat.fun/payment/success");
        payload.put("FailURL", "https://anicat.fun/payment/fail");
        payload.put("RedirectDueDate", redirectDueDate);

        Map<String, Object> receipt = Map.of(
                "Email", username + "@anicat.fun",
                "Phone", "+79998887766",
                "Taxation", "osn",
                "Items", List.of(Map.of(
                        "Name", "Пополнение баланса AniCat",
                        "Price", amount,
                        "Quantity", 1,
                        "Amount", amount,
                        "Tax", "none"
                ))
        );
        payload.put("Receipt", receipt);

        Map<String, String> data = Map.of(
                "Email", username + "@anicat.fun",
                "Phone", "+79998887766"
        );
        payload.put("DATA", data);

        String token = generateToken(payload, redirectDueDate);
        payload.put("Token", token);

        log.info("🔐 Токен успешно сгенерирован:");
        logTokenFields(payload, redirectDueDate);
        log.info("🔑 Token (SHA-256): {}", token);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(INIT_URL, entity, Map.class);
            Map<String, Object> body = response.getBody();

            log.info("📡 Ответ от Tinkoff:");
            if (body != null) {
                log.info("📦 Успешно: {}", body.get("Success"));
                log.info("📦 Код: {} | Сообщение: {}",
                        body.getOrDefault("ErrorCode", "-"),
                        body.getOrDefault("Message", "-"));
                if (body.get("Details") != null) {
                    log.info("📦 Детали: {}", body.get("Details"));
                }
            }

            long end = System.currentTimeMillis();
            log.info("⏱️ Время обработки запроса: {} мс", end - start);

            if (body != null && Boolean.TRUE.equals(body.get("Success"))) {
                return ResponseEntity.ok(Map.of(
                        "paymentUrl", body.get("PaymentURL"),
                        "orderId", orderId
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Ошибка Tinkoff", "details", body));
            }

        } catch (Exception e) {
            log.error("❌ Ошибка при соединении с Tinkoff", e);
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка соединения", "message", e.getMessage()));
        }
    }

    private String generateToken(Map<String, Object> payload, String redirectDueDate) {
        Map<String, String> tokenFields = new HashMap<>();
        tokenFields.put("TerminalKey", String.valueOf(payload.get("TerminalKey")));
        tokenFields.put("Amount", String.valueOf(payload.get("Amount")));
        tokenFields.put("OrderId", String.valueOf(payload.get("OrderId")));
        tokenFields.put("Description", String.valueOf(payload.get("Description")));
        tokenFields.put("CustomerKey", String.valueOf(payload.get("CustomerKey")));
        tokenFields.put("PayType", String.valueOf(payload.get("PayType")));
        tokenFields.put("Language", String.valueOf(payload.get("Language")));
        tokenFields.put("NotificationURL", String.valueOf(payload.get("NotificationURL")));
        tokenFields.put("SuccessURL", String.valueOf(payload.get("SuccessURL")));
        tokenFields.put("FailURL", String.valueOf(payload.get("FailURL")));
        tokenFields.put("RedirectDueDate", redirectDueDate);
        tokenFields.put("Password", PASSWORD);

        List<String> keys = new ArrayList<>(tokenFields.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(tokenFields.get(key));
        }

        return sha256(sb.toString());
    }

    private void logTokenFields(Map<String, Object> payload, String redirectDueDate) {
        Map<String, String> fields = Map.ofEntries(
                Map.entry("TerminalKey", String.valueOf(payload.get("TerminalKey"))),
                Map.entry("Amount", String.valueOf(payload.get("Amount"))),
                Map.entry("OrderId", String.valueOf(payload.get("OrderId"))),
                Map.entry("Description", String.valueOf(payload.get("Description"))),
                Map.entry("CustomerKey", String.valueOf(payload.get("CustomerKey"))),
                Map.entry("PayType", String.valueOf(payload.get("PayType"))),
                Map.entry("Language", String.valueOf(payload.get("Language"))),
                Map.entry("NotificationURL", String.valueOf(payload.get("NotificationURL"))),
                Map.entry("SuccessURL", String.valueOf(payload.get("SuccessURL"))),
                Map.entry("FailURL", String.valueOf(payload.get("FailURL"))),
                Map.entry("RedirectDueDate", redirectDueDate),
                Map.entry("Password", PASSWORD)
        );

        log.info("📑 Параметры токена:");
        fields.forEach((key, value) -> log.info("   {} = {}", key, value));
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 ошибка", e);
        }
    }
}
