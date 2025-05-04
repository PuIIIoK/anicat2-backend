package puiiiokiq.anicat.backend.episodes.Controllers;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import puiiiokiq.anicat.backend.utils.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestController
@RequestMapping("/api/anime/episodes")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/episode/{id}")
    public ResponseEntity<?> getEpisodeById(
            @PathVariable String id,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        String key = "anime/episodes/anilibria/1080p/" + id + ".mp4";

        try {
            ResponseInputStream<GetObjectResponse> s3Stream = s3Service.getFileStream(key, range);
            GetObjectResponse metadata = s3Stream.response();

            long contentLength = metadata.contentLength();
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, "video/mp4");

            if (range != null && range.startsWith("bytes=")) {
                String[] ranges = range.replace("bytes=", "").split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : contentLength - 1;
                long rangeLength = end - start + 1;

                headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeLength));
                headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength);

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new InputStreamResource(s3Stream));
            } else {
                headers.setContentLength(contentLength);
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(new InputStreamResource(s3Stream));
            }
        } catch (NoSuchKeyException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Файл не найден в хранилище: " + key);
        } catch (S3Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка доступа к S3: " + e.awsErrorDetails().errorMessage());
        }
    }
}
