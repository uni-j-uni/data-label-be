/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "SignUpResponse DTO", description = "사용자 회원가입에 대한 응답 반환")
public class UserResponse {

  @Schema(description = "사용자 식별자", example = "1")
  private Long userId;

  @Schema(description = "사용자 아이디", example = "heejun")
  private String username;

  @Schema(description = "사용자 이름", example = "윤희준")
  private String name;

  @Schema(description = "사용자 권한", example = "ROLE_ADMIN")
  private String role;
}
