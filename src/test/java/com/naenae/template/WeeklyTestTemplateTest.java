package com.naenae.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.naenae.common.pagination.PageView;
import com.naenae.student.weeklytest.model.StudentWeeklyTestListItem;
import com.naenae.teacher.student.model.CourseOption;
import com.naenae.teacher.weeklytest.model.WeeklyTestAttachmentItem;
import com.naenae.teacher.weeklytest.model.WeeklyTestDetail;
import com.naenae.teacher.weeklytest.model.WeeklyTestListItem;
import com.naenae.teacher.weeklytest.model.WeeklyTestScoreRow;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

class WeeklyTestTemplateTest {
    private SpringTemplateEngine engine;
    private IWebExchange exchange;

    @BeforeEach
    void setUp() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML"); resolver.setCharacterEncoding("UTF-8");
        engine = new SpringTemplateEngine(); engine.setTemplateResolver(resolver);
        var context = new MockServletContext();
        exchange = JakartaServletWebApplication.buildApplication(context).buildExchange(
                new MockHttpServletRequest(context), new MockHttpServletResponse());
    }

    @Test
    void teacherListRendersRegistrationAndTwoMonthSearch() {
        var values = common();
        values.put("startDate", LocalDate.of(2026, 5, 21));
        values.put("endDate", LocalDate.of(2026, 7, 21));
        values.put("weeklyTestPage", new PageView<>(List.of(new WeeklyTestListItem(
                1L, "[2026년 07월 셋째주 테스트] 중등1반", "중등1반", "92.5점", LocalDateTime.of(2026,7,21,10,0))),
                0, 1, 1, true, true, List.of(0)));

        String html = engine.process("teacher/weekly-tests", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("이번주 테스트 등록", "name=\"startDate\"", "2026-05-21",
                "[2026년 07월 셋째주 테스트] 중등1반", "(평균 92.5점)", "/teacher/weekly-tests/1");
    }

    @Test
    void teacherDetailRendersRosterScoreInputsAndDownload() {
        var values = common();
        values.put("weeklyTest", new WeeklyTestDetail(1L, "테스트", "중등1반", "3단원",
                LocalDateTime.of(2026,7,21,10,0), List.of(new WeeklyTestAttachmentItem(2L,"시험.pdf","12 KB")),
                List.of(new WeeklyTestScoreRow(3L,4L,"김학생","나래중",90))));

        String html = engine.process("teacher/weekly-test-detail", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("김학생", "name=\"score_3\"", "value=\"90\"",
                "/teacher/weekly-tests/1/attachments/2/download", "점수 저장");
    }

    @Test
    void studentListShowsOnlyAssignedTestsAndOwnScore() {
        var values = common();
        values.put("weeklyTestPage", new PageView<>(List.of(new StudentWeeklyTestListItem(
                1L,"테스트","중등1반",LocalDateTime.of(2026,7,21,10,0),95)),
                0,1,1,true,true,List.of(0)));

        String html = engine.process("student/weekly-tests", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("내 주간테스트", "95점", "/student/weekly-tests/1");
    }

    private HashMap<String, Object> common() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("teacherHasProfileImage", false); values.put("teacherDisplayName", "김선생쌤");
        values.put("studentHasProfileImage", false); values.put("studentInitial", "김");
        values.put("studentDisplayName", "김학생"); values.put("courses", List.of(new CourseOption(1L,"중등1반")));
        return values;
    }
}
