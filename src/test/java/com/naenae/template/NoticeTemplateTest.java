package com.naenae.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.naenae.common.notice.model.*;
import com.naenae.common.board.model.BoardListItem;
import com.naenae.common.pagination.PageView;
import com.naenae.student.dashboard.model.StudentDashboard;
import com.naenae.teacher.student.model.CourseOption;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

class NoticeTemplateTest {
    private SpringTemplateEngine engine;
    private IWebExchange exchange;

    @BeforeEach
    void setUp() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html"); resolver.setTemplateMode("HTML"); resolver.setCharacterEncoding("UTF-8");
        engine = new SpringTemplateEngine(); engine.setTemplateResolver(resolver);
        var servletContext = new MockServletContext();
        exchange = JakartaServletWebApplication.buildApplication(servletContext).buildExchange(
                new MockHttpServletRequest(servletContext), new MockHttpServletResponse());
    }

    @Test
    void teacherNoticeTemplatesRenderTargetsPagingAndDownloads() {
        NoticeListItem item = item();
        var list = base();
        list.put("noticePage", new PageView<>(List.of(item), 0, 2, 11, true, false, List.of(0, 1)));
        assertThat(render("teacher/notices", list)).contains("등록한 알림", "중등 A반", "/teacher/notice?page=1");

        var form = base();
        form.put("editMode", true); form.put("noticeId", 1L); form.put("title", "준비물 안내");
        form.put("contentHtml", "<p>본문</p>"); form.put("targetAll", false);
        form.put("publishStartDate", LocalDate.of(2026, 7, 16));
        form.put("publishEndDate", LocalDate.of(2026, 7, 20));
        form.put("courses", List.of(new CourseOption(2L, "중등 A반"))); form.put("selectedCourseIds", List.of(2L));
        form.put("existingAttachments", List.of(new NoticeAttachmentItem(3L, "안내.pdf", "10 KB")));
        assertThat(render("teacher/notice-form", form)).contains("전체", "중등 A반", "안내.pdf", "알림 수정",
                "게시 시작일", "게시 종료일", "2026-07-16", "2026-07-20");

        var detail = base(); detail.put("notice", detail());
        assertThat(render("teacher/notice-detail", detail)).contains("알림장 상세", "안내.pdf", "/teacher/notice/1/attachments/3/download");
    }

    @Test
    void studentNoticeTemplatesAndDashboardRenderVisibleNotices() {
        var list = base(); list.put("noticePage", new PageView<>(List.of(item()), 0, 1, 1, true, true, List.of(0)));
        assertThat(render("student/notices", list)).contains("받은 알림", "/student/notices/1");
        var detail = base(); detail.put("notice", detail());
        assertThat(render("student/notice-detail", detail)).contains("안내.pdf", "/student/notices/1/attachments/3/download");
        DashboardNoticeItem dashboardNotice = new DashboardNoticeItem(1L, LocalDateTime.of(2026,7,15,9,0),
                "준비물 안내", "중등 A반", "교재를 준비하세요.");
        BoardListItem boardPost = new BoardListItem(2L, "수업 자료", "[선생님] 나나쌤",
                LocalDateTime.of(2026,7,15,10,0), 3, 1, 0);
        var dashboard = base(); dashboard.put("studentDashboard", new StudentDashboard("김학생", "중등", "apple", "사과", "An apple.", "사과 한 개.",
                List.of(), List.of(dashboardNotice), List.of(boardPost)));
        assertThat(render("student/dashboard", dashboard)).contains("김학생 학생 대시보드", "오늘의 알림장",
                "/student/notices/1", "수업 자료", "/student/board/2");
    }

    private NoticeListItem item() { return new NoticeListItem(1L, LocalDateTime.of(2026,7,15,9,0), "준비물 안내", "중등 A반", 1); }
    private NoticeDetail detail() { return new NoticeDetail(1L, LocalDateTime.of(2026,7,15,9,0), "준비물 안내", "중등 A반", "<p>본문</p>", List.of(new NoticeAttachmentItem(3L,"안내.pdf","10 KB"))); }
    private HashMap<String,Object> base() { var values=new HashMap<String,Object>(); values.put("teacherDisplayName","테스트 선생님"); values.put("_csrf",new DefaultCsrfToken("X-CSRF-TOKEN","_csrf","token")); return values; }
    private String render(String name, HashMap<String,Object> values) { return engine.process(name,new WebContext(exchange,Locale.KOREAN,values)); }
}
