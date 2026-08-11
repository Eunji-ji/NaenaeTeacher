package com.naenae.teacher.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.auth.security.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

class PortfolioDemoLoginServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsTeacherSecuritySessionWithoutPasswordAuthentication() {
        CustomUserDetailsService userDetailsService = org.mockito.Mockito.mock(CustomUserDetailsService.class);
        User demoUser = User.createTeacher("portfolio-demo", "unusable", "체험 선생님");
        CustomUserDetails userDetails = new CustomUserDetails(demoUser);
        when(userDetailsService.loadUserByUsername("portfolio-demo")).thenReturn(userDetails);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        new PortfolioDemoLoginService(userDetailsService).login(request, response);

        Object storedContext = request.getSession(false).getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(storedContext).isSameAs(SecurityContextHolder.getContext());
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("portfolio-demo");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_TEACHER");
    }
}
