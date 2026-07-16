package com.naenae.teacher.classschedule.repository;

import com.naenae.teacher.classschedule.domain.ClassSchedule;
import com.naenae.teacher.classschedule.domain.ScheduleWeekday;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByTeacherIdOrderByWeekdayAscStartTimeAscEndTimeAsc(Long teacherId);
    Optional<ClassSchedule> findByIdAndTeacherId(Long id, Long teacherId);
    long deleteByTeacherId(Long teacherId);

    @Query("""
            select count(schedule) from ClassSchedule schedule
            where schedule.teacher.id = :teacherId
              and schedule.weekday = :weekday
              and schedule.startTime < :endTime
              and schedule.endTime > :startTime
            """)
    long countOverlapping(@Param("teacherId") Long teacherId,
                          @Param("weekday") ScheduleWeekday weekday,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime);
}
