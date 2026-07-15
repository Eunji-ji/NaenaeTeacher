package com.naenae.teacher.assignment.model;
import com.naenae.teacher.assignment.domain.AssignmentStatus;
import com.naenae.teacher.student.model.CourseOption;
import java.time.LocalDate;
import java.util.List;
public record AssignmentFormData(Long id, List<CourseOption> courses, String title, LocalDate startDate,
                                 LocalDate endDate, AssignmentStatus status, String contentHtml,
                                 List<AssignmentAttachmentItem> attachments) {}