package com.naenae.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.naenae.common.pagination.PageView;
import com.naenae.student.learning.model.StudentAttendanceItem;
import com.naenae.student.learning.model.StudentAttendancePage;
import com.naenae.student.learning.model.StudentScoreItem;
import com.naenae.student.learning.model.StudentScorePage;
import com.naenae.student.learning.model.StudentWordTestItem;
import com.naenae.student.mypage.model.StudentMyPageData;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.springframework.security.web.csrf.DefaultCsrfToken;

class StudentLearningTemplateTest {

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
    void scorePageRendersLearningSubmenusAndOrderedChanges() {
        var values = new HashMap<String, Object>();
        values.put("scorePage", new StudentScorePage("김학생", List.of(
                new StudentScoreItem(2025, "중간고사", 80, null),
                new StudentScoreItem(2025, "기말고사", 90, 10)
        ), 90, 10, 85));

        String html = engine.process("student/learning-scores", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("내 학습현황", "시험 점수 변화", "단어시험 결과", "출석률",
                "/student/learning/scores", "/student/learning/word-tests", "/student/learning/attendance",
                "2025 중간고사", "2025 기말고사", "+10점");
    }

    @Test
    void wordTestAndAttendancePagesRenderResponsiveLearningData() {
        var wordValues = new HashMap<String, Object>();
        wordValues.put("wordTestPage", page(List.of(new StudentWordTestItem(
                1L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 7), "중등 1반", 20,
                "종료", "completed", "결과 미등록"
        ))));
        String wordHtml = engine.process("student/learning-word-tests", new WebContext(exchange, Locale.KOREAN, wordValues));

        var attendanceValues = new HashMap<String, Object>();
        attendanceValues.put("attendancePage", new StudentAttendancePage(
                "김학생", 90, 10, 8, 1, 1, 0,
                page(List.of(new StudentAttendanceItem(LocalDate.of(2026, 7, 21), "중등 1반", "출석", "present")))
        ));
        String attendanceHtml = engine.process("student/learning-attendance", new WebContext(exchange, Locale.KOREAN, attendanceValues));

        assertThat(wordHtml).contains("단어 20개", "중등 1반", "결과 미등록");
        assertThat(attendanceHtml).contains("90%", "전체 기록", "지각", "결석", "2026.07.21");
    }

    @Test
    void studentSidebarAndMyPageUseRealNameAndProfileImageOnly() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("profile", new StudentMyPageData("김학생", "student01", true));
        values.put("studentDisplayName", "김학생");
        values.put("studentInitial", "김");
        values.put("studentHasProfileImage", true);

        String html = engine.process("student/mypage", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("김학생 학생", "Student Profile", "student01",
                "href=\"/student/mypage\"", "/student/mypage/profile-image",
                "name=\"profileImage\"", "프로필 사진 저장");
        assertThat(html).doesNotContain("name=\"nickname\"");
    }

    private <T> PageView<T> page(List<T> content) {
        return new PageView<>(content, 0, 1, content.size(), true, true, List.of(0));
    }
}
