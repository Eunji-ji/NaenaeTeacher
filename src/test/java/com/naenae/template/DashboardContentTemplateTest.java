package com.naenae.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.naenae.common.board.model.BoardListItem;
import com.naenae.common.notice.model.DashboardNoticeItem;
import com.naenae.teacher.todayenglish.model.TodayEnglishWordCounts;
import com.naenae.teacher.todayenglish.model.TodayEnglishWordItem;
import com.naenae.teacher.todayenglish.model.TodayEnglishSentenceCounts;
import com.naenae.teacher.todayenglish.model.TodayEnglishSentenceItem;
import com.naenae.teacher.classschedule.domain.ScheduleWeekday;
import com.naenae.teacher.classschedule.model.ClassScheduleItem;
import com.naenae.common.pagination.PageView;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.model.TodayWordView;
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

class DashboardContentTemplateTest {
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
    void teacherDashboardRendersLiveNoticeBoardAndMyPageLink() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 16, 9, 30);
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("teacherDisplayName", "나나쌤");
        values.put("teacherName", "나나");
        values.put("teacherInitial", "나");
        values.put("teacherHasProfileImage", true);
        values.put("totalStudentCount", 10);
        values.put("todayPresentCount", 8);
        values.put("todayLateCount", 1);
        values.put("todayAbsentCount", 1);
        values.put("todayAttendanceRate", 90);
        values.put("openAssignmentCount", 2);
        values.put("todayEnglishWords", List.of(new TodayWordView(
                WordLevel.LOWER_ELEMENTARY, "초등저학년", "apple", "사과")));
        values.put("todayEnglishSentences", List.of());
        values.put("todayNotice", new DashboardNoticeItem(4L, now, "오늘 준비물", "중등 A반", "교재를 준비하세요."));
        values.put("boardPosts", List.of(new BoardListItem(7L, "수업 자료", "[선생님] 나나쌤", now, 3, 2, 1)));
        values.put("todaySchedules", List.of(new ClassScheduleItem(9L, 2L, "중등 A반",
                ScheduleWeekday.THURSDAY, "목요일", "중등 문법", LocalTime.of(16, 0), LocalTime.of(17, 0))));

        String html = engine.process("teacher/dashboard", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("오늘 준비물", "교재를 준비하세요.", "/teacher/notice/4",
                "수업 자료", "/teacher/board/7", "href=\"/teacher/mypage\"",
                "href=\"/teacher/today-english\" aria-label=\"오늘의 영어 관리로 이동\"", "apple", "사과",
                "중등 문법", "중등 A반", "/teacher/classes/schedule");
        assertThat(occurrences(html, "/teacher/mypage/profile-image")).isEqualTo(2);
        assertThat(occurrences(html, "나나")).isGreaterThanOrEqualTo(2);
        assertThat(html).doesNotContain("class=\"mobile-menu-button\"");
    }

    @Test
    void todayEnglishPageRendersLevelHeadersUploadAndClassManagementMenu() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("teacherDisplayName", "나나쌤");
        values.put("teacherHasProfileImage", false);
        values.put("wordCounts", new TodayEnglishWordCounts(3, 5, 4));

        String html = engine.process("teacher/today-english", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("LEVEL1", "LEVEL2", "LEVEL3", "단어 · 뜻", "/teacher/today-english/template",
                "/teacher/today-english/upload", "단어등록", "단어조회", "/teacher/today-english/words",
                "문장등록", "문장조회", "/teacher/today-english/sentences/upload",
                "/teacher/today-english/sentences", "수업관리", "/teacher/classes/progress", "/teacher/classes/schedule");
    }

    @Test
    void todayEnglishWordQueryRendersLevelFilterPagingAndDelete() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("teacherDisplayName", "나나쌤");
        values.put("teacherHasProfileImage", false);
        values.put("wordLevels", WordLevel.values());
        values.put("selectedLevel", WordLevel.LOWER_ELEMENTARY);
        values.put("wordPaginationUrl", "/teacher/today-english/words?level=LOWER_ELEMENTARY");
        values.put("wordPage", new PageView<>(List.of(new TodayEnglishWordItem(
                8L, WordLevel.LOWER_ELEMENTARY, "초등저학년", "apple", "사과")),
                0, 2, 11, true, false, List.of(0, 1)));

        String html = engine.process("teacher/today-english-words", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("apple", "사과", "초등저학년",
                "/teacher/today-english/words/8/delete", "level=LOWER_ELEMENTARY", "page=1");
    }

    @Test
    void todayEnglishSentenceUploadRendersLevelSheetsAndTemplate() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("teacherDisplayName", "나나쌤");
        values.put("teacherHasProfileImage", false);
        values.put("sentenceCounts", new TodayEnglishSentenceCounts(2, 3, 4));

        String html = engine.process("teacher/today-english-sentences-upload",
                new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("문장등록", "LEVEL1", "LEVEL2", "LEVEL3", "문장 · 뜻",
                "/teacher/today-english/sentences/template", "/teacher/today-english/sentences/upload",
                "문장조회", ">2</span>개", ">3</span>개", ">4</span>개");
    }

    @Test
    void todayEnglishSentenceQueryRendersMeaningPagingAndDelete() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("teacherDisplayName", "나나쌤");
        values.put("teacherHasProfileImage", false);
        values.put("sentenceLevels", WordLevel.values());
        values.put("selectedLevel", WordLevel.MIDDLE_SCHOOL);
        values.put("sentencePaginationUrl", "/teacher/today-english/sentences?level=MIDDLE_SCHOOL");
        values.put("sentencePage", new PageView<>(List.of(new TodayEnglishSentenceItem(
                12L, WordLevel.MIDDLE_SCHOOL, "중학생", "Small efforts matter.", "작은 노력이 중요해요.")),
                0, 2, 11, true, false, List.of(0, 1)));

        String html = engine.process("teacher/today-english-sentences",
                new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("문장조회", "Small efforts matter.", "작은 노력이 중요해요.", "중학생",
                "/teacher/today-english/sentences/12/delete", "level=MIDDLE_SCHOOL", "page=1");
    }

    private int occurrences(String text, String value) {
        return (text.length() - text.replace(value, "").length()) / value.length();
    }
}
