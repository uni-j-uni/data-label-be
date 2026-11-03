/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "AssignmentCreateRequest DTO", description = "이미지를 작업자에게 할당하기 위한 데이터 전송")
public class AssignmentCreateRequest {

  @NotNull(message = "작업자 식별자는 필수입니다.") @Schema(description = "작업을 배정할 작업자(ROLE_USER) 식별자", example = "2")
  private Long workerId;

  /** 빈 목록은 서비스에서 NO_IMAGE_SELECTED(ASGN4005)로 거부하므로 Bean Validation 을 두지 않음 */
  @Schema(description = "할당할 이미지 식별자 목록", example = "[1, 2, 3]")
  private List<Long> imageIds;
}
