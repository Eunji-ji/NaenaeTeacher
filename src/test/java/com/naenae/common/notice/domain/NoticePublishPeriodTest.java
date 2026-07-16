package com.naenae.common.notice.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.naenae.teacher.profile.domain.Teacher;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

class NoticePublishPeriodTest {
    @Test
    void storesAndUpdatesPublishPeriod() {
        LocalDate start = LocalDate.of(2026, 7, 16);
        LocalDate end = LocalDate.of(2026, 7, 20);
        Notice notice = Notice.create(mock(Teacher.class), "안내", "<p>내용</p>", true, start, end);

        assertThat(notice.getPublishStartDate()).isEqualTo(start);
        assertThat(notice.getPublishEndDate()).isEqualTo(end);

        notice.update("수정", "<p>수정</p>", true, start.plusDays(1), end.plusDays(1));
        assertThat(notice.getPublishStartDate()).isEqualTo(start.plusDays(1));
        assertThat(notice.getPublishEndDate()).isEqualTo(end.plusDays(1));
    }
}
