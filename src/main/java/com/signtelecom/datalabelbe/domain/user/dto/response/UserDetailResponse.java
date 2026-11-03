/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "UserDetailResponse DTO", description = "사용자 정보 응답 반환")
public class UserDetailResponse {

  @Schema(description = "식별자", example = "1")
  private Long userId;

  @Schema(description = "아이디", example = "heejun")
  private String username;

  @Schema(description = "이름", example = "윤희준")
  private String name;

  @Schema(description = "권한", example = "ROLE_USER")
  private String role;
}
