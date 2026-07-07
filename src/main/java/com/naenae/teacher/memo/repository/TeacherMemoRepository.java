package com.naenae.teacher.memo.repository;

import com.naenae.teacher.memo.domain.TeacherMemo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherMemoRepository extends JpaRepository<TeacherMemo, Long> {
}
