package puiiiokiq.anicat.backend.episodes.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/kinescope")
public class KinescopeController {

    @Value("${kinescope.api.token}")
    private String kinescopeApiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/search")
    public ResponseEntity<String> searchVideo(@RequestParam String title) {
        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = "https://api.kinescope.io/v1/videos?search=" + encodedTitle;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + kinescopeApiToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Ошибка запроса к Kinescope API (видео)\"}");
        }
    }

    @GetMapping("/search-playlist")
    public ResponseEntity<String> searchPlaylist(@RequestParam String title) {
        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = "https://api.kinescope.io/v1/playlists?search=" + encodedTitle;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + kinescopeApiToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Ошибка запроса к Kinescope API (плейлист)\"}");
        }
    }
}
