package com.naenae.common.notice.model;
import com.naenae.teacher.student.model.CourseOption;
import java.util.List;
import java.time.LocalDate;
public record NoticeFormData(Long id, String title, String contentHtml, boolean targetAll,
                             LocalDate publishStartDate, LocalDate publishEndDate,
                             List<CourseOption> selectedCourses, List<NoticeAttachmentItem> attachments) {}
