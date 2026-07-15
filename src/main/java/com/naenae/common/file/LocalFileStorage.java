package com.naenae.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalFileStorage {

    public StoredFile store(Path storageRoot, MultipartFile file) {
        Path normalizedRoot = storageRoot.toAbsolutePath().normalize();
        String originalName = Path.of(Objects.requireNonNullElse(file.getOriginalFilename(), "file"))
                .getFileName().toString();
        String storedName = UUID.randomUUID() + extension(originalName);
        Path target = normalizedRoot.resolve(storedName).normalize();
        if (!target.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("올바르지 않은 파일명입니다.");
        }
        try {
            Files.createDirectories(normalizedRoot);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("파일 저장에 실패했습니다.", exception);
        }
        return new StoredFile(originalName, storedName, file.getContentType(), file.getSize());
    }

    public Path resolveExisting(Path storageRoot, String storedName) {
        Path normalizedRoot = storageRoot.toAbsolutePath().normalize();
        Path file = normalizedRoot.resolve(storedName).normalize();
        if (!file.startsWith(normalizedRoot) || !Files.isRegularFile(file)) {
            throw new IllegalStateException("파일이 저장소에 존재하지 않습니다.");
        }
        return file;
    }

    public void deleteIfExists(Path storageRoot, String storedName) {
        Path normalizedRoot = storageRoot.toAbsolutePath().normalize();
        Path file = normalizedRoot.resolve(storedName).normalize();
        if (!file.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("올바르지 않은 저장 파일명입니다.");
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new IllegalStateException("파일 삭제에 실패했습니다.", exception);
        }
    }
    private String extension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || fileName.length() - index > 12) {
            return "";
        }
        return fileName.substring(index).toLowerCase(Locale.ROOT).replaceAll("[^.a-z0-9]", "");
    }
}