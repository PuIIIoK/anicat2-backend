package puiiiokiq.anicat.backend.anime.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import puiiiokiq.anicat.backend.anime.models.AnimeNotAllowedCountry;
import puiiiokiq.anicat.backend.anime.Repository.AnimeNotAllowedCountryRepository;

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
        // Здесь необходимо интегрировать сервис для определения страны по IP.
        // Например, использовать MaxMind GeoIP2 или сторонний API.
        // Для примера вернём "RU" как заглушку.
        return "RU";
    }

    public boolean isAnimeAccessible(Long animeId) {
        String countryCode = countryService.getUserCountryCode();
        return notAllowedRepo.findByAnimeId(animeId).stream()
                .noneMatch(entry -> entry.getCountry().equalsIgnoreCase(countryCode));
    }
}
