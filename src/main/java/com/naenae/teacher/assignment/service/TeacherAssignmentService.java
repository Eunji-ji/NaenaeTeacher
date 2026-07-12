package com.naenae.teacher.assignment.service;

import com.naenae.teacher.assignment.domain.Assignment;
import com.naenae.teacher.assignment.model.AssignmentListItem;
import com.naenae.teacher.assignment.repository.AssignmentRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherAssignmentService {
    private static final int MAX_FILES = 5;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final Path uploadRoot;

    public TeacherAssignmentService(TeacherRepository t, CourseRepository c, AssignmentRepository a,
                                    @Value("${app.upload.assignment-dir:uploads/assignments}") String uploadDir) {
        teacherRepository=t; courseRepository=c; assignmentRepository=a; uploadRoot=Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly=true)
    public List<AssignmentListItem> getAssignments(Long userId) {
        Teacher teacher=getTeacher(userId);
        return assignmentRepository.findByTeacherIdOrderByStartDateDescIdDesc(teacher.getId()).stream()
                .map(a->new AssignmentListItem(a.getId(),a.getStartDate(),a.getEndDate(),a.getCourses().stream()
                        .map(x->x.getCourse().getTitle()).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.joining(", ")),
                        a.getTitle(),a.getAttachments().size()))
                .sorted(Comparator.comparing(AssignmentListItem::startDate).reversed().thenComparing(AssignmentListItem::courseNames,String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public void create(Long userId,List<Long> courseIds,String title,LocalDate start,LocalDate end,String html,List<MultipartFile> files) {
        Teacher teacher=getTeacher(userId); List<Course> courses=getCourses(teacher,courseIds);
        String cleanTitle=require(title,"과제 제목을 입력해 주세요.");
        if(start==null||end==null||end.isBefore(start)) throw new IllegalArgumentException("과제 게시 기간을 확인해 주세요.");
        String cleanHtml=Jsoup.clean(html==null?"":html,Safelist.relaxed().removeTags("img"));
        List<MultipartFile> actual=files==null?List.of():files.stream().filter(f->!f.isEmpty()).toList();
        if(actual.size()>MAX_FILES) throw new IllegalArgumentException("첨부파일은 최대 5개까지 등록할 수 있습니다.");
        actual.forEach(f->{if(f.getSize()>MAX_FILE_SIZE)throw new IllegalArgumentException("첨부파일은 파일당 10MB 이하여야 합니다.");});
        Assignment assignment=Assignment.create(teacher,cleanTitle,cleanHtml,start,end); courses.forEach(assignment::addCourse);
        try { Files.createDirectories(uploadRoot);
            for(MultipartFile f:actual){String original=Path.of(Objects.requireNonNullElse(f.getOriginalFilename(),"file")).getFileName().toString();String stored=UUID.randomUUID()+extension(original);Path target=uploadRoot.resolve(stored).normalize();if(!target.startsWith(uploadRoot))throw new IllegalArgumentException("올바르지 않은 파일명입니다.");Files.copy(f.getInputStream(),target,StandardCopyOption.REPLACE_EXISTING);assignment.addAttachment(original,stored,f.getContentType(),f.getSize());}
        } catch(IOException e){throw new IllegalStateException("첨부파일 저장에 실패했습니다.",e);}
        assignmentRepository.save(assignment);
    }
    private Teacher getTeacher(Long id){return teacherRepository.findByUserId(id).orElseThrow(()->new IllegalStateException("선생님 정보를 찾을 수 없습니다."));}
    private List<Course> getCourses(Teacher t,List<Long> ids){if(ids==null||ids.isEmpty())throw new IllegalArgumentException("과제를 등록할 반을 선택해 주세요.");List<Long>d=new ArrayList<>(new LinkedHashSet<>(ids));List<Course>c=courseRepository.findByTeacherIdAndIdInOrderByTitleAsc(t.getId(),d);if(c.size()!=d.size())throw new IllegalArgumentException("선택한 반 정보를 확인할 수 없습니다.");return c;}
    private String require(String v,String m){if(v==null||v.trim().isEmpty())throw new IllegalArgumentException(m);return v.trim();}
    private String extension(String n){int i=n.lastIndexOf('.');if(i<0||n.length()-i>12)return "";return n.substring(i).toLowerCase(Locale.ROOT).replaceAll("[^.a-z0-9]","");}
}
