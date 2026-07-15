package com.naenae.common.board.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.common.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Entity @Table(name = "board_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardComment extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "post_id", nullable = false) private BoardPost post;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_user_id", nullable = false) private User author;
    @Column(nullable = false, length = 1000) private String content;
    static BoardComment create(BoardPost post, User author, String content) {
        BoardComment value = new BoardComment(); value.post = post; value.author = author; value.content = content; return value;
    }
    public void update(String content) { this.content = content; }
}