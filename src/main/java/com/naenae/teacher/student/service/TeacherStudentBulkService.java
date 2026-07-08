package com.naenae.teacher.student.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherStudentBulkService {

    private static final List<String> EXPECTED_HEADERS = List.of("학생이름", "반", "학교", "전화번호");

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final TeacherStudentService teacherStudentService;

    public TeacherStudentBulkService(
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            TeacherStudentService teacherStudentService
    ) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.teacherStudentService = teacherStudentService;
    }

    @Transactional(readOnly = true)
    public byte[] createTemplate(Long teacherUserId) {
        getTeacher(teacherUserId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("학생등록");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
                headerRow.createCell(i).setCellValue(EXPECTED_HEADERS.get(i));
            }

            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("홍길동");
            sampleRow.createCell(1).setCellValue("초등 A반");
            sampleRow.createCell(2).setCellValue("내곡초");
            sampleRow.createCell(3).setCellValue("010-1234-5678");

            for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("템플릿 파일을 생성할 수 없습니다.", exception);
        }
    }

    @Transactional
    public BulkImportResult importStudents(Long teacherUserId, MultipartFile file) {
        Teacher teacher = getTeacher(teacherUserId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일을 선택해 주세요.");
        }

        int createdCount = 0;
        List<String> errors = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            validateHeaders(sheet);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, formatter)) {
                    continue;
                }

                String studentName = readCell(row, 0, formatter);
                String courseTitle = readCell(row, 1, formatter);
                String schoolName = readCell(row, 2, formatter);
                String phone = readCell(row, 3, formatter);

                if (studentName == null || courseTitle == null) {
                    errors.add((rowIndex + 1) + "행: 학생이름과 반은 필수입니다.");
                    continue;
                }

                Course course = courseRepository.findFirstByTeacherIdAndTitleIgnoreCase(teacher.getId(), courseTitle)
                        .orElse(null);
                if (course == null) {
                    errors.add((rowIndex + 1) + "행: '" + courseTitle + "' 반을 찾을 수 없습니다.");
                    continue;
                }

                teacherStudentService.createStudent(teacherUserId, studentName, course.getId(), schoolName, phone);
                createdCount++;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("엑셀 파일을 읽을 수 없습니다.", exception);
        }

        return new BulkImportResult(createdCount, errors);
    }

    private Teacher getTeacher(Long teacherUserId) {
        return teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private void validateHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("엑셀 첫 줄에 헤더가 필요합니다.");
        }

        DataFormatter formatter = new DataFormatter();
        for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
            String actual = readCell(headerRow, i, formatter);
            if (!EXPECTED_HEADERS.get(i).equals(actual)) {
                throw new IllegalArgumentException("헤더는 '학생이름, 반, 학교, 전화번호' 순서여야 합니다.");
            }
        }
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        for (int i = 0; i < 4; i++) {
            if (readCell(row, i, formatter) != null) {
                return false;
            }
        }
        return true;
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record BulkImportResult(
            int createdCount,
            List<String> errors
    ) {
    }
}
