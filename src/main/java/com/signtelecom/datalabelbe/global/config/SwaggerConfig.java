/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Value("${swagger.server.profile}")
  private String profileUrl;

  @Value("${swagger.server.name}")
  private String profileName;

  @Bean
  public OpenAPI customOpenAPI() {
    Server server = new Server().url(profileUrl + contextPath).description(profileName + " Server");

    return new OpenAPI()
        // 서버 정보
        .addServersItem(server)

        // 전역 JWT 보안 설정
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))

        // SecurityScheme 정의
        .components(
            new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))

        // API 기본 정보
        .info(
            new Info()
                .title("데이터 라벨링 웹 시스템 API 명세서")
                .version("v1.0.0")
                .description(
                    """
                    ## 주의사항
                    - 파일 업로드 크기 제한: 10MB (1개 파일 기준)
                    """)
                .contact(new Contact().name("SIGNTELECOM").email("hjyoon@signtelecom.com")));
  }

  // 그룹 이름은 Swagger UI 드롭다운 정렬 순서용, 화면에는 displayName 표시

  /** 전체 API */
  @Bean
  public GroupedOpenApi allApiGroup() {
    return GroupedOpenApi.builder()
        .group("1-all")
        .displayName("전체")
        .pathsToMatch("/api/**")
        .build();
  }

  /** 일반 사용자(작업자)가 호출할 수 있는 API. 관리자 전용 API 제외 */
  @Bean
  public GroupedOpenApi userApiGroup() {
    return GroupedOpenApi.builder()
        .group("2-user")
        .displayName("사용자")
        .pathsToMatch("/api/**")
        .pathsToExclude("/api/admin/**")
        .build();
  }

  /** 관리자 전용 API. 토큰 발급을 위해 인증 API 포함 */
  @Bean
  public GroupedOpenApi adminApiGroup() {
    return GroupedOpenApi.builder()
        .group("3-admin")
        .displayName("관리자")
        .pathsToMatch("/api/admin/**", "/api/auths/**")
        .build();
  }
}
