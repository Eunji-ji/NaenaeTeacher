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
                "name=\"portal\" value=\"student\"", "href=\"/\"");
    }
}
