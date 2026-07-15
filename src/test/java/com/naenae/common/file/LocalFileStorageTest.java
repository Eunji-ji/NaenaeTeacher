package com.naenae.common.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    private final LocalFileStorage storage = new LocalFileStorage();

    @Test
    void storesAndResolvesFileInsideConfiguredRoot() throws Exception {
        var upload = new MockMultipartFile("file", "lesson.xlsx", "application/octet-stream", "data".getBytes());

        StoredFile stored = storage.store(tempDir, upload);
        Path resolved = storage.resolveExisting(tempDir, stored.storedName());

        assertThat(stored.originalName()).isEqualTo("lesson.xlsx");
        assertThat(resolved).startsWith(tempDir);
        assertThat(Files.readString(resolved)).isEqualTo("data");
    }

    @Test
    void rejectsDownloadPathOutsideConfiguredRoot() {
        assertThatThrownBy(() -> storage.resolveExisting(tempDir, "../secret.txt"))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void deletesStoredFileInsideConfiguredRoot() {
        var upload = new MockMultipartFile("file", "lesson.txt", "text/plain", "data".getBytes());
        StoredFile stored = storage.store(tempDir, upload);

        storage.deleteIfExists(tempDir, stored.storedName());

        assertThat(tempDir.resolve(stored.storedName())).doesNotExist();
    }
}