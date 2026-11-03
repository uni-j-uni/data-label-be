/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.global.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import com.signtelecom.datalabelbe.global.security.CustomUserDetails;
import com.signtelecom.datalabelbe.global.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CorsConfig corsConfig;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * 보안 필터 체인 Bean
   *
   * <p>HttpSecurity의 설정 메서드와 build()가 throws Exception으로 선언되어 있어 이 메서드에서만 선언한다.
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    try {
      http.csrf(AbstractHttpConfigurer::disable)
          .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
          .exceptionHandling(this::configureExceptionHandling)
          .authorizeHttpRequests(this::configureAuthorization);
      return http.build();
    } catch (RuntimeException e) {
      log.error("Security configuration failed", e);
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** 예외 처리: 인증 실패와 권한 부족 처리 */
  private void configureExceptionHandling(ExceptionHandlingConfigurer<HttpSecurity> e) {
    e.authenticationEntryPoint(this::handleAuthException)
        .accessDeniedHandler(this::handleAccessDenied);
  }

  private void handleAuthException(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response
        .getWriter()
        .write("{\"success\": false, \"code\": 401, \"message\": \"JWT 토큰이 없거나 유효하지 않습니다.\"}");
    log.warn("인증 실패: {} {}", request.getMethod(), request.getRequestURI());
  }

  private void handleAccessDenied(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json;charset=UTF-8");

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = "anonymous";

    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
      Object principal = auth.getPrincipal();
      if (principal instanceof CustomUserDetails) {
        userId = ((CustomUserDetails) principal).user().getId().toString();
      }
    }

    response
        .getWriter()
        .write("{\"success\": false, \"code\": 403, \"message\": \"접근 권한이 없습니다.\"}");
    log.warn("권한 부족: {} {}, userId={}", request.getMethod(), request.getRequestURI(), userId);
  }

  /** 권한 설정 */
  private void configureAuthorization(
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
          auth) {
    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/")
        .permitAll()

        // 인증 관련 엔드포인트
        .requestMatchers(
            "/api/users/sign-up", "/api/auths/login", "/api/auths/refresh", "/actuator/health")
        .permitAll()

        // 연동용 API
        .requestMatchers("/api/example")
        .permitAll()

        // 관리자 전용 API는 모두 /api/admin 아래에 둠
        .requestMatchers("/api/admin/**")
        .hasRole("ADMIN")
        .requestMatchers(RegexRequestMatcher.regexMatcher(".*/dev(/.*)?"))
        .hasRole("DEVELOPER")
        .anyRequest()
        .authenticated();
  }

  /** 비밀번호 인코더 Bean */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** 인증 관리자 Bean */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
