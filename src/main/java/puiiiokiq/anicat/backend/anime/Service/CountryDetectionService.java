package puiiiokiq.anicat.backend.anime.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;

@Service
public class CountryDetectionService {

    public String getUserCountryCode() {
        try {
            URL url = new URL("https://ipapi.co/json/");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String json = reader.readLine();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            return node.get("country").asText(); // RU, KZ, US и т.д.
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
