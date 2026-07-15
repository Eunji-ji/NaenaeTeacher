package com.naenae.teacher.assignment.model;

import java.nio.file.Path;

public record AssignmentDownload(Path path, String originalName, String contentType) {
}