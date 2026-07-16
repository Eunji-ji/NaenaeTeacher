package com.naenae.common.board.service;

import com.naenae.common.board.domain.*;
import com.naenae.common.board.model.*;
import com.naenae.common.board.repository.BoardPostRepository;
import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.file.StoredFile;
import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.common.user.domain.Role;
import com.naenae.common.user.domain.User;
import com.naenae.common.user.repository.UserRepository;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BoardService {
    private static final int MAX_FILES = 5;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final BoardPostRepository postRepository;
    private final LocalFileStorage fileStorage;
    private final Path storageRoot;

    public BoardService(UserRepository userRepository, TeacherRepository teacherRepository,
                        StudentRepository studentRepository, CourseStudentRepository courseStudentRepository,
                        BoardPostRepository postRepository, LocalFileStorage fileStorage,
                        @Value("${app.storage.board-dir}") String storageDir) {
        this.userRepository = userRepository; this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository; this.courseStudentRepository = courseStudentRepository;
        this.postRepository = postRepository; this.fileStorage = fileStorage;
        this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public PageView<BoardListItem> getPosts(Long userId, int page) {
        Actor actor = actor(userId);
        return PaginationSupport.toView(postRepository.findByTeacherIdOrderByCreatedAtDescIdDesc(
                actor.teacher().getId(), PaginationSupport.pageRequest(page)).map(this::toListItem));
    }

    @Transactional(readOnly = true)
    public List<BoardListItem> getRecentPosts(Long userId, int size) {
        Actor actor = actor(userId);
        return postRepository.findByTeacherIdOrderByCreatedAtDescIdDesc(
                        actor.teacher().getId(), PageRequest.of(0, size))
                .map(this::toListItem).getContent();
    }

    @Transactional
    public BoardDetail getPost(Long userId, Long postId) {
        Actor actor = actor(userId); BoardPost post = visible(actor, postId); post.increaseViewCount();
        return detail(actor, post);
    }

    @Transactional(readOnly = true)
    public BoardFormData getForm(Long userId, Long postId) {
        Actor actor = actor(userId); BoardPost post = visible(actor, postId); requireAuthor(actor, post);
        return new BoardFormData(post.getId(), post.getTitle(), post.getContentHtml(), attachments(post));
    }

    @Transactional
    public Long create(Long userId, String title, String html, List<MultipartFile> files) {
        Actor actor = actor(userId); Values values = values(title, html); List<MultipartFile> actual = files(files, 0);
        BoardPost post = BoardPost.create(actor.teacher(), actor.user(), values.title(), values.html());
        saveFiles(post, actual); return postRepository.save(post).getId();
    }

    @Transactional
    public void update(Long userId, Long postId, String title, String html, List<MultipartFile> files) {
        Actor actor = actor(userId); BoardPost post = visible(actor, postId); requireAuthor(actor, post);
        Values values = values(title, html); List<MultipartFile> actual = files(files, post.getAttachments().size());
        post.update(values.title(), values.html()); saveFiles(post, actual);
    }

    @Transactional
    public void delete(Long userId, Long postId) {
        Actor actor = actor(userId); BoardPost post = visible(actor, postId);
        if (!canDelete(actor, post)) throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
        List<String> names = post.getAttachments().stream().map(BoardAttachment::getStoredName).toList();
        postRepository.delete(post); deleteAfterCommit(names);
    }

    @Transactional
    public void addComment(Long userId, Long postId, String content) {
        Actor actor = actor(userId); BoardPost post = visible(actor, postId);
        post.addComment(actor.user(), comment(content));
    }

    @Transactional
    public void deleteComment(Long userId, Long postId, Long commentId) {
        Actor actor = actor(userId); BoardPost post = visible(actor, postId);
        BoardComment comment = post.getComments().stream().filter(item -> item.getId().equals(commentId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!comment.getAuthor().getId().equals(actor.user().getId()) && actor.user().getRole() != Role.TEACHER)
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        post.removeComment(comment);
    }

    @Transactional(readOnly = true)
    public BoardDownload download(Long userId, Long postId, Long attachmentId) {
        BoardPost post = visible(actor(userId), postId);
        BoardAttachment attachment = post.getAttachments().stream().filter(item -> item.getId().equals(attachmentId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));
        return new BoardDownload(fileStorage.resolveExisting(storageRoot, attachment.getStoredName()),
                attachment.getOriginalName(), attachment.getContentType());
    }

    private BoardDetail detail(Actor actor, BoardPost post) {
        List<BoardCommentItem> comments = post.getComments().stream()
                .sorted(Comparator.comparing(BoardComment::getCreatedAt).thenComparing(BoardComment::getId))
                .map(comment -> new BoardCommentItem(comment.getId(), authorLabel(comment.getAuthor()), comment.getContent(),
                        comment.getCreatedAt(), comment.getAuthor().getId().equals(actor.user().getId()) || actor.user().getRole() == Role.TEACHER))
                .toList();
        boolean author = post.getAuthor().getId().equals(actor.user().getId());
        return new BoardDetail(post.getId(), post.getTitle(), authorLabel(post.getAuthor()), post.getCreatedAt(),
                post.getViewCount(), post.getContentHtml(), attachments(post), comments, author, canDelete(actor, post));
    }

    private BoardListItem toListItem(BoardPost post) {
        return new BoardListItem(post.getId(), post.getTitle(), authorLabel(post.getAuthor()), post.getCreatedAt(),
                post.getViewCount(), post.getComments().size(), post.getAttachments().size());
    }

    private String authorLabel(User user) {
        if (user.getRole() == Role.TEACHER) return "[선생님] " + displayName(user);
        Optional<Student> student = studentRepository.findByUserId(user.getId());
        if (student.isEmpty()) return "[학생] " + displayName(user);
        String courses = courseStudentRepository.findByStudent_IdOrderByCourseTitleAsc(student.get().getId()).stream()
                .map(mapping -> mapping.getCourse().getTitle()).distinct().collect(Collectors.joining(", "));
        return "[" + (courses.isBlank() ? "미배정" : courses) + "] " + displayName(user);
    }

    private String displayName(User user) {
        return user.getNickname() == null || user.getNickname().isBlank() ? user.getName() : user.getNickname();
    }

    private List<BoardAttachmentItem> attachments(BoardPost post) {
        return post.getAttachments().stream().sorted(Comparator.comparing(BoardAttachment::getId))
                .map(file -> new BoardAttachmentItem(file.getId(), file.getOriginalName(), size(file.getFileSize()))).toList();
    }

    private Actor actor(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
        if (user.getRole() == Role.TEACHER) {
            Teacher teacher = teacherRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
            return new Actor(user, teacher);
        }
        if (user.getRole() == Role.STUDENT) {
            Student student = studentRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("학생 정보를 찾을 수 없습니다."));
            return new Actor(user, student.getTeacher());
        }
        throw new IllegalArgumentException("게시판을 이용할 수 없는 계정입니다.");
    }

    private BoardPost visible(Actor actor, Long postId) {
        return postRepository.findByIdAndTeacherId(postId, actor.teacher().getId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }
    private void requireAuthor(Actor actor, BoardPost post) {
        if (!post.getAuthor().getId().equals(actor.user().getId())) throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
    }
    private boolean canDelete(Actor actor, BoardPost post) {
        return post.getAuthor().getId().equals(actor.user().getId()) || actor.user().getRole() == Role.TEACHER;
    }
    private Values values(String title, String html) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("제목을 입력해 주세요.");
        String clean = Jsoup.clean(html == null ? "" : html, Safelist.relaxed().removeTags("img"));
        if (Jsoup.parse(clean).text().isBlank()) throw new IllegalArgumentException("내용을 입력해 주세요.");
        return new Values(title.trim(), clean);
    }
    private String comment(String content) {
        String value = content == null ? "" : content.trim();
        if (value.isEmpty()) throw new IllegalArgumentException("댓글 내용을 입력해 주세요.");
        if (value.length() > 1000) throw new IllegalArgumentException("댓글은 1000자 이하로 입력해 주세요.");
        return value;
    }
    private List<MultipartFile> files(List<MultipartFile> files, int existing) {
        List<MultipartFile> actual = files == null ? List.of() : files.stream().filter(file -> !file.isEmpty()).toList();
        if (existing + actual.size() > MAX_FILES) throw new IllegalArgumentException("첨부파일은 기존 파일을 포함해 최대 5개까지 등록할 수 있습니다.");
        actual.forEach(file -> { if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("첨부파일은 파일당 10MB 이하여야 합니다."); });
        return actual;
    }
    private void saveFiles(BoardPost post, List<MultipartFile> files) {
        files.forEach(file -> { StoredFile stored = fileStorage.store(storageRoot, file);
            post.addAttachment(stored.originalName(), stored.storedName(), stored.contentType(), stored.size()); });
    }
    private void deleteAfterCommit(List<String> names) {
        Runnable cleanup = () -> names.forEach(name -> fileStorage.deleteIfExists(storageRoot, name));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) { cleanup.run(); return; }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { cleanup.run(); }
        });
    }
    private String size(long bytes) {
        if (bytes >= 1048576) return String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
        if (bytes >= 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return bytes + " B";
    }
    private record Actor(User user, Teacher teacher) {}
    private record Values(String title, String html) {}
}
