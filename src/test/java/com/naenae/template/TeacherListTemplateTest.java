package com.naenae.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.naenae.common.pagination.PageView;
import com.naenae.teacher.assignment.model.AssignmentAttachmentItem;
import com.naenae.teacher.assignment.model.AssignmentDetail;
import com.naenae.teacher.assignment.model.AssignmentListItem;
import com.naenae.teacher.student.model.CourseOption;
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

class TeacherListTemplateTest {

    private SpringTemplateEngine templateEngine;
    private IWebExchange exchange;

    @BeforeEach
    void setUp() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        var servletContext = new MockServletContext();
        var application = JakartaServletWebApplication.buildApplication(servletContext);
        exchange = application.buildExchange(
                new MockHttpServletRequest(servletContext),
                new MockHttpServletResponse()
        );
    }

    @Test
    void assignmentAndWordTestListsRenderSharedPaginationFragment() {
        var variables = baseVariables();
        variables.put("courses", List.of());
        variables.put("assignmentPage", new PageView<>(
                List.of(new AssignmentListItem(
                        1L,
                        LocalDateTime.of(2026, 7, 15, 9, 0),
                        LocalDate.of(2026, 7, 15),
                        LocalDate.of(2026, 7, 22),
                        "중등 A반",
                        "숙제",
                        3
                )),
                0, 3, 21, true, false, List.of(0, 1, 2)
        ));
        String assignments = templateEngine.process("teacher/assignments", context(variables));

        variables.remove("assignmentPage");
        variables.put("wordTestPage", pagedResult());
        String wordTests = templateEngine.process("teacher/word-tests", context(variables));

        assertThat(assignments).contains(
                "등록한 과제",
                "📎 첨부파일 3개",
                "수정",
                "삭제",
                "오늘의 영어",
                "알림장",
                "게시판",
                "설정",
                "/teacher/assignments?page=1"
        );
        assertThat(wordTests).contains("등록한 단어시험", "오늘의 영어", "알림장", "게시판", "설정", "/teacher/word-tests?page=1");
    }

    @Test
    void assignmentEditFormRendersExistingValuesAndAttachment() {
        var variables = baseVariables();
        variables.put("editMode", true);
        variables.put("assignmentId", 1L);
        variables.put("selectedCourses", List.of(new CourseOption(2L, "중등 A반")));
        variables.put("title", "수정할 과제");
        variables.put("startDate", LocalDate.of(2026, 7, 15));
        variables.put("endDate", LocalDate.of(2026, 7, 22));
        variables.put("descriptionHtml", "<p>본문</p>");
        variables.put("existingAttachments", List.of(new AssignmentAttachmentItem(3L, "숙제.xlsx", "10 KB")));

        String form = templateEngine.process("teacher/assignment-form", context(variables));

        assertThat(form).contains("과제 수정", "수정할 과제", "숙제.xlsx", "/teacher/assignments/1");
    }
    @Test
    void assignmentDetailRendersDownloadSection() {
        var variables = baseVariables();
        variables.put("assignment", new AssignmentDetail(
                1L,
                LocalDateTime.of(2026, 7, 15, 9, 0),
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 22),
                "중등 A반",
                "숙제",
                "<p>본문</p>",
                List.of()
        ));

        String detail = templateEngine.process("teacher/assignment-detail", context(variables));

        assertThat(detail).contains("과제 상세조회", "첨부파일", "등록된 첨부파일이 없습니다.");
    }

    private HashMap<String, Object> baseVariables() {
        var variables = new HashMap<String, Object>();
        variables.put("teacherDisplayName", "테스트 선생님");
        variables.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        return variables;
    }

    private PageView<Object> pagedResult() {
        return new PageView<>(List.of(), 0, 3, 21, true, false, List.of(0, 1, 2));
    }

    private WebContext context(HashMap<String, Object> variables) {
        return new WebContext(exchange, Locale.KOREAN, variables);
    }
}