package com.naenae.student.assignment.service;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.assignment.domain.*;
import com.naenae.teacher.assignment.model.*;
import com.naenae.teacher.assignment.repository.AssignmentRepository;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentAssignmentService {
    private final StudentRepository studentRepository; private final AssignmentRepository assignmentRepository;
    private final LocalFileStorage fileStorage; private final Path storageRoot;
    public StudentAssignmentService(StudentRepository studentRepository, AssignmentRepository assignmentRepository,
                                    LocalFileStorage fileStorage, @Value("${app.storage.assignment-dir}") String dir) {
        this.studentRepository=studentRepository;this.assignmentRepository=assignmentRepository;this.fileStorage=fileStorage;
        this.storageRoot=Path.of(dir).toAbsolutePath().normalize();
    }
    @Transactional(readOnly=true) public PageView<AssignmentListItem> getAssignments(Long userId,int page){Student student=student(userId);return PaginationSupport.toView(assignmentRepository.findStudentAssignments(student.getId(),AssignmentStatus.IN_PROGRESS,PaginationSupport.pageRequest(page)).map(this::item));}
    @Transactional(readOnly=true) public List<AssignmentListItem> getRecentAssignments(Student student,int size){return assignmentRepository.findStudentAssignments(student.getId(),AssignmentStatus.IN_PROGRESS,PageRequest.of(0,size)).map(this::item).getContent();}
    @Transactional(readOnly=true) public AssignmentDetail getAssignment(Long userId,Long id){Assignment a=visible(student(userId),id);return new AssignmentDetail(a.getId(),a.getCreatedAt(),a.getStartDate(),a.getEndDate(),courses(a),a.getTitle(),a.getContentHtml(),files(a));}
    @Transactional(readOnly=true) public AssignmentDownload download(Long userId,Long id,Long fileId){Assignment a=visible(student(userId),id);AssignmentAttachment f=a.getAttachments().stream().filter(x->x.getId().equals(fileId)).findFirst().orElseThrow(()->new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));return new AssignmentDownload(fileStorage.resolveExisting(storageRoot,f.getStoredName()),f.getOriginalName(),f.getContentType());}
    private Assignment visible(Student s,Long id){return assignmentRepository.findStudentVisibleAssignment(id,s.getId(),AssignmentStatus.IN_PROGRESS).orElseThrow(()->new IllegalArgumentException("과제를 찾을 수 없습니다."));}
    private Student student(Long userId){return studentRepository.findByUserId(userId).orElseThrow(()->new IllegalStateException("학생 정보를 찾을 수 없습니다."));}
    private AssignmentListItem item(Assignment a){return new AssignmentListItem(a.getId(),a.getCreatedAt(),a.getStartDate(),a.getEndDate(),courses(a),a.getTitle(),a.getStatus(),a.getStatus().getLabel(),a.getAttachments().size());}
    private String courses(Assignment a){return a.getCourses().stream().map(x->x.getCourse().getTitle()).sorted().collect(Collectors.joining(", "));}
    private List<AssignmentAttachmentItem> files(Assignment a){return a.getAttachments().stream().map(f->new AssignmentAttachmentItem(f.getId(),f.getOriginalName(),size(f.getFileSize()))).toList();}
    private String size(long b){if(b>=1048576)return String.format(Locale.ROOT,"%.1f MB",b/1048576.0);if(b>=1024)return String.format(Locale.ROOT,"%.1f KB",b/1024.0);return b+" B";}
}