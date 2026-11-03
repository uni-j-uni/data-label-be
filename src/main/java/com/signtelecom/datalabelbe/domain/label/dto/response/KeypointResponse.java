/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.dto.response;

import com.signtelecom.datalabelbe.domain.label.entity.KeypointGroup;
import com.signtelecom.datalabelbe.domain.label.entity.KeypointType;
import com.signtelecom.datalabelbe.domain.label.entity.Visibility;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "KeypointResponse DTO", description = "키포인트 하나의 좌표와 가시성")
public class KeypointResponse {

  @Schema(description = "COCO 17 키포인트 종류", example = "LEFT_SHOULDER")
  private KeypointType type;

  @Schema(description = "COCO keypoints 배열에서의 순서", example = "5")
  private int cocoIndex;

  @Schema(description = "키포인트 이름(한글)", example = "왼쪽 어깨")
  private String label;

  @Schema(description = "신체 부위 그룹. 화면에서 얼굴/상체/하체로 묶어 보여준다", example = "UPPER_BODY")
  private KeypointGroup group;

  @Schema(description = "x 좌표", example = "150.0")
  private Double x;

  @Schema(description = "y 좌표", example = "200.0")
  private Double y;

  @Schema(description = "가시성", example = "VISIBLE")
  private Visibility visibility;

  @Schema(description = "COCO v 값 (0 미표시 / 1 가려짐 / 2 보임)", example = "2")
  private int visibilityCode;
}
