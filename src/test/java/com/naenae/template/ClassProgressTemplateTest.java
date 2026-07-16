package com.naenae.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.naenae.common.pagination.PageView;
import com.naenae.teacher.classprogress.domain.ProgressNoteColor;
import com.naenae.teacher.classprogress.model.ClassProgressNoteItem;
import com.naenae.teacher.classprogress.model.ProgressScheduleOption;
import com.naenae.teacher.student.model.CourseOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

class ClassProgressTemplateTest {
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
    void rendersOptionalFilteredScheduleFormAndStickyNotes() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("teacherDisplayName", "나나쌤");
        values.put("teacherHasProfileImage", false);
        values.put("courses", List.of(new CourseOption(2L, "중등1반")));
        values.put("schedules", List.of(new ProgressScheduleOption(
                3L, 2L, "월요일", LocalTime.of(16, 0), LocalTime.of(17, 0), "영어문법")));
        values.put("selectedCourseId", null);
        values.put("selectedScheduleId", null);
        values.put("memo", "");
        values.put("progressPage", new PageView<>(List.of(
                new ClassProgressNoteItem(9L, "중등1반", "영어문법", ProgressNoteColor.YELLOW, "현재완료까지 진행",
                        LocalDateTime.of(2026, 7, 16, 10, 0)),
                new ClassProgressNoteItem(10L, null, null, ProgressNoteColor.LIGHT_BLUE, "교재 주문하기",
                        LocalDateTime.of(2026, 7, 16, 9, 0))),
                0, 2, 8, true, false, List.of(0, 1)));
        values.put("progressPaginationUrl", "/teacher/classes/progress");

        String html = engine.process("teacher/class-progress", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("maxlength=\"1000\"", "data-course-id=\"2\"",
                "월요일 16:00~17:00 · 영어문법", "[중등1반]", "[영어문법]",
                "진도 관리 목록", "총 8개의 메모", "sticky-yellow", "sticky-blue",
                "현재완료까지 진행", "자유 메모", "교재 주문하기", "page=1",
                "/teacher/classes/progress/9/delete", "이 진도 메모를 삭제할까요?",
                "/assets/js/class-progress.js");
    }
}
