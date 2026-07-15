package com.naenae.common.board.model;
import java.nio.file.Path;
public record BoardDownload(Path path, String originalName, String contentType) {}