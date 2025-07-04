package puiiiokiq.anicat.backend.anime.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import puiiiokiq.anicat.backend.anime.models.AnimeNotAllowedCountry;
import puiiiokiq.anicat.backend.anime.Repository.AnimeNotAllowedCountryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimeAccessService {

    @Autowired
    private AnimeNotAllowedCountryRepository animeNotAllowedCountryRepository;
    private final AnimeNotAllowedCountryRepository notAllowedRepo;
    private final CountryDetectionService countryService;

    public boolean isAnimeAccessible(Long animeId, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String countryCode = getCountryCodeByIp(clientIp);

        List<AnimeNotAllowedCountry> blockedCountries = animeNotAllowedCountryRepository.findByAnimeId(animeId);
        List<String> blockedCountryCodes = blockedCountries.stream()
                .map(AnimeNotAllowedCountry::getCountry)
                .collect(Collectors.toList());

        return !blockedCountryCodes.contains(countryCode);
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private String getCountryCodeByIp(String ip) {
        try {
            // Игнорируем локальные/зарезервированные адреса
            if (ip.startsWith("127.") || ip.equals("::1") || ip.startsWith("192.168") || ip.startsWith("10.") || ip.startsWith("172.")) {
                return "RU"; // fallback по умолчанию
            }

            URL url = new URL("https://ipwho.is/" + ip);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.toString());

            boolean success = json.path("success").asBoolean();
            if (success) {
                return json.path("country_code").asText(); // Пример: "RU"
            } else {
                System.err.println("GeoIP error: " + json.path("message").asText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "UNKNOWN";
    }


    public boolean isAnimeAccessible(Long animeId, String ip) {
        String countryCode = getCountryCodeByIp(ip);
        List<AnimeNotAllowedCountry> blockedCountries = animeNotAllowedCountryRepository.findByAnimeId(animeId);
        List<String> blockedCountryCodes = blockedCountries.stream()
                .map(AnimeNotAllowedCountry::getCountry)
                .collect(Collectors.toList());

        return !blockedCountryCodes.contains(countryCode);
    }




    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // если пришёл список через запятую — берём первый (клиентский)
            return ip.split(",")[0];
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr(); // fallback
    }

}
