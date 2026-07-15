package com.naenae.teacher.assignment.model;
import com.naenae.teacher.assignment.domain.AssignmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record AssignmentListItem(Long id, LocalDateTime createdAt, LocalDate startDate, LocalDate endDate,
                                 String courseNames, String title, AssignmentStatus status, String statusLabel,
                                 int attachmentCount) {}