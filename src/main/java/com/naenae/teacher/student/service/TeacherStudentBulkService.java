package com.naenae.teacher.student.service;

import com.naenae.common.excel.ExcelFileService;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherStudentBulkService {

    private static final List<String> EXPECTED_HEADERS = List.of("학생이름", "반", "학교", "전화번호");
    private static final List<String> SAMPLE_VALUES = List.of("홍길동", "초등 A반", "내내초", "010-1234-5678");

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final TeacherStudentService teacherStudentService;
    private final ExcelFileService excelFileService;

    public TeacherStudentBulkService(
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            TeacherStudentService teacherStudentService,
            ExcelFileService excelFileService
    ) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.teacherStudentService = teacherStudentService;
        this.excelFileService = excelFileService;
    }

    @Transactional(readOnly = true)
    public byte[] createTemplate(Long teacherUserId) {
        getTeacher(teacherUserId);
        return excelFileService.createTemplate("학생등록", EXPECTED_HEADERS, SAMPLE_VALUES);
    }

    @Transactional
    public BulkImportResult importStudents(Long teacherUserId, MultipartFile file) {
        Teacher teacher = getTeacher(teacherUserId);
        int createdCount = 0;
        List<String> errors = new ArrayList<>();

        for (ExcelFileService.ExcelRow row : excelFileService.readRows(file, EXPECTED_HEADERS)) {
            String studentName = row.value(0);
            String courseTitle = row.value(1);
            String schoolName = row.value(2);
            String phone = row.value(3);

            if (studentName == null || courseTitle == null) {
                errors.add(row.rowNumber() + "행: 학생이름과 반은 필수입니다.");
                continue;
            }

            Course course = courseRepository.findFirstByTeacherIdAndTitleIgnoreCase(teacher.getId(), courseTitle)
                    .orElse(null);
            if (course == null) {
                errors.add(row.rowNumber() + "행: '" + courseTitle + "' 반을 찾을 수 없습니다.");
                continue;
            }

            teacherStudentService.createStudent(teacherUserId, studentName, course.getId(), schoolName, phone);
            createdCount++;
        }
        return new BulkImportResult(createdCount, errors);
    }

    private Teacher getTeacher(Long teacherUserId) {
        return teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    public record BulkImportResult(int createdCount, List<String> errors) {
    }
}