package com.naenae.common.file;

public record StoredFile(
        String originalName,
        String storedName,
        String contentType,
        long size
) {
}