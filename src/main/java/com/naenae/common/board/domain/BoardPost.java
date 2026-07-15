package com.naenae.common.board.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.common.user.domain.User;
import com.naenae.teacher.profile.domain.Teacher;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "board_posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardPost extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "teacher_id", nullable = false) private Teacher teacher;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_user_id", nullable = false) private User author;
    @Column(nullable = false, length = 200) private String title;
    @Column(name = "content_html", nullable = false, columnDefinition = "TEXT") private String contentHtml;
    @Column(name = "view_count", nullable = false) private long viewCount;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<BoardAttachment> attachments = new ArrayList<>();
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<BoardComment> comments = new ArrayList<>();

    public static BoardPost create(Teacher teacher, User author, String title, String contentHtml) {
        BoardPost post = new BoardPost(); post.teacher = teacher; post.author = author;
        post.title = title; post.contentHtml = contentHtml; return post;
    }
    public void update(String title, String contentHtml) { this.title = title; this.contentHtml = contentHtml; }
    public void increaseViewCount() { viewCount++; }
    public void addAttachment(String originalName, String storedName, String contentType, long fileSize) {
        attachments.add(BoardAttachment.create(this, originalName, storedName, contentType, fileSize));
    }
    public void addComment(User author, String content) { comments.add(BoardComment.create(this, author, content)); }
    public void removeComment(BoardComment comment) { comments.remove(comment); }
}