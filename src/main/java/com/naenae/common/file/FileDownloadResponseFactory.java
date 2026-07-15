package com.naenae.common.file;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FileDownloadResponseFactory {

    public ResponseEntity<Resource> create(Path path, String originalName, String contentType) {
        FileSystemResource resource = new FileSystemResource(path);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(originalName, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(toMediaType(contentType))
                .contentLength(path.toFile().length())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    private MediaType toMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}