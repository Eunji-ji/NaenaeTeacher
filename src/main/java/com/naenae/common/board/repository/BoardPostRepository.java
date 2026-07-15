package com.naenae.common.board.repository;
import com.naenae.common.board.domain.BoardPost;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
    Page<BoardPost> findByTeacherIdOrderByCreatedAtDescIdDesc(Long teacherId, Pageable pageable);
    Optional<BoardPost> findByIdAndTeacherId(Long postId, Long teacherId);
}