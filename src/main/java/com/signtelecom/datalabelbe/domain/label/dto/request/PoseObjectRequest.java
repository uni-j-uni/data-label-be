/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
@Schema(title = "PoseObjectRequest DTO", description = "인물 한 명의 바운딩 박스와 키포인트 17개")
public class PoseObjectRequest {

  @Valid
  @NotNull(message = "바운딩 박스는 필수입니다.") @Schema(description = "인물 바운딩 박스")
  private BoundingBoxRequest bbox;

  @Valid
  @NotEmpty(message = "키포인트는 필수입니다.")
  @Schema(description = "COCO 17 키포인트. 종류 17개가 빠짐없이, 중복 없이 들어와야 한다")
  private List<KeypointRequest> keypoints;
}
