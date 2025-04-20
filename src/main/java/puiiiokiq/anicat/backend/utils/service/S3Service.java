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
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // Получение потока файла с поддержкой Range (битрейтной загрузки)
    public ResponseInputStream<GetObjectResponse> getFileStream(String key, String range) {
        GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key);

        if (range != null && !range.isEmpty()) {
            requestBuilder.range(range);
        }

        return s3Client.getObject(requestBuilder.build());
    }

    public ResponseInputStream<GetObjectResponse> getFileStream(String key) {
        return getFileStream(key, null);
    }

    // Генерация временной ссылки на объект (Presigned URL)
    public URL generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(60))
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }

    public String generatePresignedCoverUrl(Long animeId, Long coverId) {
        String key = "animes/" + animeId + "/cover/" + coverId + ".webp";
        return generatePresignedUrl(key).toString();
    }

    public boolean fileExists(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            } else {
                throw new RuntimeException("Ошибка при обращении к S3", e);
            }
        }
    }

    public void uploadFile(String key, MultipartFile file) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentDisposition("inline")
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке файла в S3", e);
        }
    }

    public void uploadMultipartFile(MultipartFile file, String key) throws IOException {
        uploadFile(key, file);
    }

    public void createAnimeDirectories(Long animeId) {
        createEmptyFolder("animes/" + animeId + "/cover/");
        createEmptyFolder("animes/" + animeId + "/episodes/");
        createEmptyFolder("animes/" + animeId + "/screenshots/");
    }

    private void createEmptyFolder(String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key.endsWith("/") ? key : key + "/")
                .build();

        s3Client.putObject(request, RequestBody.empty());
    }

    public void deleteAnimeDirectory(Long animeId) {
        String prefix = "animes/" + animeId + "/";
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix);

            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(requestBuilder.build());

            for (S3Object object : listResponse.contents()) {
                String key = object.key();
                System.out.println("🧹 Удаляем: " + key);
                try {
                    s3Client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build());
                } catch (S3Exception e) {
                    System.err.println("⚠️ Ошибка при удалении " + key + ": " + e.awsErrorDetails().errorMessage());
                }
            }

            continuationToken = listResponse.nextContinuationToken();
        } while (continuationToken != null);

        System.out.println("✅ Удаление аниме " + animeId + " завершено.");
    }


    private RuntimeException throwAsRuntime(S3Exception e) {
        throw new RuntimeException("Ошибка при обращении к S3", e);
    }

    public void uploadInputStream(InputStream inputStream, long contentLength, String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("image/webp") // так как ты сохраняешь .webp
                .contentDisposition("inline")
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(inputStream, contentLength));
    }

    public URL generatePresignedUploadUrl(String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("video/mp4") // обязательно, если хочешь передавать его в запросе
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url();
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    public void deleteFolderRecursive(String prefix) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

        List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                .toList();

        if (!objectsToDelete.isEmpty()) {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();

            s3Client.deleteObjects(deleteRequest);
        }
    }

}
