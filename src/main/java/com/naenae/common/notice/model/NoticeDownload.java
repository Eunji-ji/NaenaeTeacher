package com.naenae.common.notice.model;
import java.nio.file.Path;
public record NoticeDownload(Path path, String originalName, String contentType) {}