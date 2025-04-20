package puiiiokiq.anicat.backend.utils.configs;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfigElement {

    @Bean
    public jakarta.servlet.MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofGigabytes(5));     // 5 GB
        factory.setMaxRequestSize(DataSize.ofGigabytes(5));  // 5 GB
        return factory.createMultipartConfig();
    }
}
