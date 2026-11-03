/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 키포인트 가시성. COCO keypoints 규격의 v 값과 1:1로 대응
 *
 * <p>DB에는 읽기 쉽도록 이름으로 저장하고, COCO export 시 {@link #getCode()}의 정수를 씀
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "키포인트 가시성 Enum (COCO v 값)")
public enum Visibility {
  @Schema(description = "표시하지 않음 (COCO v=0)")
  NOT_LABELED(0, "미표시"),
  @Schema(description = "위치는 찍었으나 가려져 보이지 않음 (COCO v=1)")
  OCCLUDED(1, "가려짐"),
  @Schema(description = "보임 (COCO v=2)")
  VISIBLE(2, "보임");

  private final int code;
  private final String ko;

  /** 이 키포인트가 실제로 찍힌 것으로 볼지 여부. 완료 수 집계에 사용 */
  public boolean isLabeled() {
    return this != NOT_LABELED;
  }
}
