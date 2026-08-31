package com.quietjun.ssafymng.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // REST API 및 파일 업로드를 위해 비활성화
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/index.html",
                    "/assignment",
                    "/speaker",
                    "/settings",
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/icons/**",
                    "/sounds/**",
                    "/uploads/**",
                    "/favicon.ico",
                    "/vite.svg",
                    "/api/auth/login",
                    "/api/auth/current",
                    "/api/auth/users",
                    "/api/speaker/layout"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/problems/**", "/api/problems", "/api/platforms/**", "/api/platforms").permitAll()
                .requestMatchers("/api/platforms/**", "/api/platforms").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/students/**", "/api/metadata/**", "/api/backup/**", "/api/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/problems").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/problems/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/problems/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/submissions/monitoring/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/")
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("sno")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":true, \"message\":\"로그인 성공\"}");
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false, \"message\":\"학번 또는 비밀번호가 올바르지 않습니다.\"}");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":true, \"message\":\"로그아웃 되었습니다.\"}");
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false, \"message\":\"로그인이 필요합니다.\"}");
                })
            );

        return http.build();
    }
}
