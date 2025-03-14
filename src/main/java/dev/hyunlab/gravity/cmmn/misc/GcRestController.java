package dev.hyunlab.gravity.cmmn.misc;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriUtils;

public class GcRestController {
  /** 파일 다운로드 */
  protected ResponseEntity<Resource> createResponseEntity(File file, String filename) throws MalformedURLException {
    if (file == null || !file.exists()) {
      return ResponseEntity.notFound().build();
    }

    Resource resource = new UrlResource(file.toPath().toUri());

    return createResponseEntity(resource, filename);
  }

  /** 파일 다운로드 */
  protected ResponseEntity<Resource> createResponseEntity(Path path, String filename) throws MalformedURLException {
    return createResponseEntity(path.toFile(), filename);
  }

  /** 파일 다운로드 */
  protected ResponseEntity<Resource> createResponseEntity(Resource resource, String filename)
      throws MalformedURLException {
    String encodedFilename = UriUtils.encode(filename, StandardCharsets.UTF_8);

    return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(encodedFilename))
        .body(resource);
  }
}
