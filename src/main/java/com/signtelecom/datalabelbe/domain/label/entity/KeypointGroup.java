/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 키포인트를 묶는 신체 부위 그룹 */
@Getter
@RequiredArgsConstructor
@Schema(description = "키포인트 신체 부위 그룹 Enum")
public enum KeypointGroup {
  @Schema(description = "얼굴")
  FACE("얼굴"),
  @Schema(description = "상체")
  UPPER_BODY("상체"),
  @Schema(description = "하체")
  LOWER_BODY("하체");

  private final String ko;
}
