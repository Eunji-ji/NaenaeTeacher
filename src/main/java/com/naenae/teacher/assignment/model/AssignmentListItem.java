package com.naenae.teacher.assignment.model;
import java.time.LocalDate;
public record AssignmentListItem(Long id, LocalDate startDate, LocalDate endDate, String courseNames, String title, int attachmentCount) {}
