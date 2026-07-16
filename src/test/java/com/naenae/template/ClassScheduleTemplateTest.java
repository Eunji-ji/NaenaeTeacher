package com.naenae.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.naenae.teacher.classschedule.domain.ScheduleWeekday;
import com.naenae.teacher.classschedule.model.ClassScheduleItem;
import com.naenae.teacher.classschedule.model.ScheduleDayColumn;
import com.naenae.teacher.student.model.CourseOption;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

class ClassScheduleTemplateTest {
    private SpringTemplateEngine engine;
    private IWebExchange exchange;

    @BeforeEach
    void setUp() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var servletContext = new MockServletContext();
        exchange = JakartaServletWebApplication.buildApplication(servletContext).buildExchange(
                new MockHttpServletRequest(servletContext), new MockHttpServletResponse());
    }

    @Test
    void rendersWeekdayScheduleAndConfirmedBulkDelete() {
        var item = new ClassScheduleItem(9L, 2L, "중등 A반", ScheduleWeekday.MONDAY,
                "월요일", "중등 문법", LocalTime.of(16, 0), LocalTime.of(17, 0));
        List<ScheduleDayColumn> days = Arrays.stream(ScheduleWeekday.values())
                .map(day -> new ScheduleDayColumn(day, day.getLabel(), day.getShortLabel(),
                        day == ScheduleWeekday.MONDAY ? List.of(item) : List.of()))
                .toList();
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("teacherDisplayName", "나나쌤");
        values.put("teacherHasProfileImage", false);
        values.put("courses", List.of(new CourseOption(2L, "중등 A반")));
        values.put("weekdays", ScheduleWeekday.values());
        values.put("selectedWeekday", ScheduleWeekday.MONDAY);
        values.put("selectedCourseId", 2L);
        values.put("startTime", LocalTime.of(16, 0));
        values.put("endTime", LocalTime.of(17, 0));
        values.put("lessonTitle", "");
        values.put("scheduleDays", days);

        String html = engine.process("teacher/class-schedule", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("월요일", "화요일", "수요일", "목요일", "금요일",
                "중등 A반", "중등 문법", "16:00", "17:00",
                "/teacher/classes/schedule/9/delete", "/teacher/classes/schedule/all/delete",
                "등록된 시간표를 모두 삭제할까요?", "이 작업은 되돌릴 수 없습니다.");
    }
}
