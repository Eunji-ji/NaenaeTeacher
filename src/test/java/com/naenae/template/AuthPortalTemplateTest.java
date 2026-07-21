package com.naenae.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
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
import com.naenae.teacher.mypage.model.MyPageData;

class AuthPortalTemplateTest {
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
    void rootPortalOffersTeacherAndStudentLoginChoices() {
        String html = engine.process("auth/home", new WebContext(exchange, Locale.KOREAN));

        assertThat(html).contains("NaenaeTeacher", "선생님용", "학생용",
                "href=\"/teacher/login\"", "href=\"/student/login\"",
                "Teacher Dashboard", "Student Dashboard");
        assertThat(html).doesNotContain("href=\"/teacher/dashboard\"", "href=\"/student/dashboard\"");
    }

    @Test
    void studentLoginPostsToSharedAuthenticationWithStudentPortalMarker() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));

        String html = engine.process("auth/student-login", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("학생 로그인", "action=\"/auth/login\"",
                "name=\"portal\" value=\"student\"", "name=\"loginId\"",
                "href=\"/student/signup\"", "회원가입", "href=\"/\"");
    }

    @Test
    void studentSignupConnectsToTeacherRegisteredStudentInformation() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));

        String html = engine.process("auth/student-signup", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("학생 회원가입", "action=\"/student/signup\"",
                "name=\"invitationCode\"", "name=\"courseId\"", "name=\"studentId\"",
                "name=\"loginId\"", "중복 확인", "name=\"password\"", "name=\"passwordConfirm\"",
                "name=\"termsAgreed\"", "name=\"privacyAgreed\"", "name=\"ageOrGuardianConfirmed\"",
                "/assets/js/student-signup.js", "href=\"/student/login\"");
    }

    @Test
    void teacherSignupRequiresSeparateLegalConsents() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));

        String html = engine.process("auth/signup", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("name=\"termsAgreed\"", "name=\"privacyAgreed\"",
                "href=\"/terms\"", "href=\"/privacy\"");
    }

    @Test
    void publicLegalDocumentsRenderOperatorAndContactDetails() {
        var values = new HashMap<String, Object>();
        values.put("serviceName", "NaenaeTeacher");
        values.put("operatorName", "테스트 운영자");
        values.put("contactEmail", "privacy@example.com");
        values.put("documentVersion", "2026-07-21");

        String terms = engine.process("legal/terms", new WebContext(exchange, Locale.KOREAN, values));
        String privacy = engine.process("legal/privacy", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(terms).contains("이용약관", "테스트 운영자", "privacy@example.com");
        assertThat(privacy).contains("개인정보처리방침", "만 14세 미만 학생", "privacy@example.com");
    }

    @Test
    void teacherMyPageOffersInvitationCodeManagement() {
        var values = new HashMap<String, Object>();
        values.put("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        values.put("profile", new MyPageData(
                "김선생", "teacher@example.com", "나나", false,
                "ABCDEFGHJKLMNPQRSTUV2345", java.time.LocalDateTime.of(2026, 8, 20, 12, 0), 100));
        values.put("teacherInitial", "나");
        values.put("teacherDisplayName", "나나쌤");
        values.put("teacherHasProfileImage", false);

        String html = engine.process("teacher/mypage", new WebContext(exchange, Locale.KOREAN, values));

        assertThat(html).contains("학생 초대코드", "value=\"ABCDEFGHJKLMNPQRSTUV2345\"", "readonly",
                "action=\"/teacher/mypage/invitation-code/reissue\"", "가입 100회 남음");
    }
}
