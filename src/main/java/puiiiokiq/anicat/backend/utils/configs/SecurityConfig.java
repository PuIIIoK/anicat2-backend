package puiiiokiq.anicat.backend.utils.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import puiiiokiq.anicat.backend.utils.JwtAuthFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/get-profile",
                                "/api/profile/**",
                                "/api/profile",
                                "/api/anime/**",
                                "/api/anime/category",
                                "/api/anime/category/**",
                                "/api/stream/**",
                                "/api/stream/",
                                "/api/anime/episodes",
                                "/api/anime/episodes/**",
                                "/api/auth/set-profile/**",
                                "/api/auth/set-profile-id/**",
                                "/api/upload/**",
                                "/api/auth/set-login/**",
                                "/api/auth/set-login-id/**",
                                "/api/kinescope/**",
                                "/api/libria/**",
                                "/api/upload/profile/**",
                                "/api/profiles/**",
                                "/api/collection/**",
                                "/api/payment/**",
                                "/api/admin/update-category/**",
                                "/api/admin/users/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/check", "/api/auth/get-role", "/api/admin/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
