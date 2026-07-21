package com.naenae.teacher.weeklytest.model;

import java.nio.file.Path;

public record WeeklyTestDownload(Path path, String originalName, String contentType) {
}
