package puiiiokiq.anicat.backend.utils.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // ✅ Получение потока файла с поддержкой Range (битрейтной загрузки)
    public ResponseInputStream<GetObjectResponse> getFileStream(String bucket, String key, String range) {
        GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key);

        if (range != null && !range.isEmpty()) {
            requestBuilder.range(range); // Пример: "bytes=0-1023"
        }

        return s3Client.getObject(requestBuilder.build());
    }

    // ✅ Генерация временной ссылки на объект (Presigned URL)
    public URL generatePresignedUrl(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(60))
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }
    public boolean fileExists(String bucketName, String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            // 404 — файл не найден
            if (e.statusCode() == 404) {
                return false;
            }
            throw e; // если это другая ошибка — пробрасываем дальше
        }
    }

    public void uploadFile(String bucketName, String key, MultipartFile file) {
        try {
            String contentType = file.getContentType(); // Получаем тип файла

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentDisposition("inline") // Важно: чтобы браузер отображал изображение
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке файла в S3", e);
        }
    }



}
