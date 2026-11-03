/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "AssignmentReleaseRequest DTO", description = "이미지의 작업 할당을 해제하기 위한 데이터 전송")
public class AssignmentReleaseRequest {

  /** 빈 목록은 서비스에서 NO_IMAGE_SELECTED(ASGN4005)로 거부하므로 Bean Validation 을 두지 않음 */
  @Schema(description = "할당을 해제할 이미지 식별자 목록", example = "[1, 2]")
  private List<Long> imageIds;
}
