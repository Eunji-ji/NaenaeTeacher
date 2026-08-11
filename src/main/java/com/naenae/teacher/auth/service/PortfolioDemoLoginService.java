package com.naenae.teacher.auth.service;

import com.naenae.common.user.domain.Role;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.auth.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class PortfolioDemoLoginService {

    static final String DEMO_LOGIN_ID = "portfolio-demo";

    private final CustomUserDetailsService userDetailsService;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public PortfolioDemoLoginService(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void login(HttpServletRequest request, HttpServletResponse response) {
        CustomUserDetails userDetails;
        try {
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(DEMO_LOGIN_ID);
        } catch (UsernameNotFoundException exception) {
            throw new IllegalStateException("현재 체험계정을 사용할 수 없습니다.", exception);
        }
        if (!userDetails.isEnabled() || userDetails.getUser().getRole() != Role.TEACHER) {
            throw new IllegalStateException("현재 체험계정을 사용할 수 없습니다.");
        }

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
