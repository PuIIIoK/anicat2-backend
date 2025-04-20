package puiiiokiq.anicat.backend.utils.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import puiiiokiq.anicat.backend.utils.service.S3Service;


import javax.imageio.spi.IIORegistry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImageService {

    private final S3Service s3Service;

    public ImageService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public String convertAndUploadImageToWebp(MultipartFile file, String keyWithoutExtension) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(1024, 1024)
                .outputFormat("webp") // это работает с twelvemonkeys.imageio-webp
                .toOutputStream(os);

        byte[] webpBytes = os.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(webpBytes);

        String finalKey = keyWithoutExtension + ".webp";

        s3Service.uploadInputStream(inputStream, webpBytes.length, finalKey);

        return finalKey;
    }
}