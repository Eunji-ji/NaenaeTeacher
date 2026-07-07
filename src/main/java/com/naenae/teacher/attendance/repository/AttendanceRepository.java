package com.naenae.teacher.attendance.repository;

import com.naenae.teacher.attendance.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
}
