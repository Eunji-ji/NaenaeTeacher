package com.naenae.common.notice.model;
import com.naenae.teacher.student.model.CourseOption;
import java.util.List;
public record NoticeFormData(Long id, String title, String contentHtml, boolean targetAll, List<CourseOption> selectedCourses, List<NoticeAttachmentItem> attachments) {}