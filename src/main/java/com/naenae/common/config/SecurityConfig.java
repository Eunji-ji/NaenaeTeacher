package com.naenae.common.config;

import com.naenae.teacher.auth.security.CustomUserDetailsService;
import com.naenae.teacher.auth.security.RoleBasedAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final RoleBasedAuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            RoleBasedAuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.roleBasedAuthenticationSuccessHandler = roleBasedAuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/teacher/login",
                                "/teacher/signup",
                                "/student",
                                "/api/auth/**",
                                "/api/health",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/assets/**",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers("/teacher/**", "/api/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/student/**", "/api/student/**").hasRole("STUDENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/teacher/login")
                        .loginProcessingUrl("/teacher/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(roleBasedAuthenticationSuccessHandler)
                        .failureUrl("/teacher/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/teacher/login?logout")
                        .permitAll()
                )
                .authenticationProvider(daoAuthenticationProvider());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
