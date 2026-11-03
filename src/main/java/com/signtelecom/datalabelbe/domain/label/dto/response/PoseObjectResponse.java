/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "PoseObjectResponse DTO", description = "인물 한 명의 바운딩 박스와 키포인트")
public class PoseObjectResponse {

  @Schema(description = "객체 식별자", example = "1")
  private Long objectId;

  @Schema(description = "화면 표시 순서. 0부터 시작하므로 'Person 1'은 index 0", example = "0")
  private Integer index;

  @Schema(description = "인물 바운딩 박스")
  private BoundingBoxResponse bbox;

  @Schema(description = "COCO 17 키포인트. cocoIndex 순으로 정렬된다")
  private List<KeypointResponse> keypoints;

  @Schema(description = "실제로 찍힌(가시성 > 0) 키포인트 수. 화면의 '17/17' 표시에 쓴다", example = "17")
  private long labeledKeypointCount;
}
