/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.signtelecom.datalabelbe.domain.label.entity.KeypointType;
import com.signtelecom.datalabelbe.domain.label.entity.Visibility;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "KeypointRequest DTO", description = "키포인트 하나의 좌표와 가시성")
public class KeypointRequest {

  @NotNull(message = "키포인트 종류는 필수입니다.") @Schema(description = "COCO 17 키포인트 종류", example = "LEFT_SHOULDER")
  private KeypointType type;

  @NotNull(message = "x 좌표는 필수입니다.") @PositiveOrZero(message = "x 좌표는 0 이상이어야 합니다.")
  @Schema(description = "x 좌표", example = "150.0")
  private Double x;

  @NotNull(message = "y 좌표는 필수입니다.") @PositiveOrZero(message = "y 좌표는 0 이상이어야 합니다.")
  @Schema(description = "y 좌표", example = "200.0")
  private Double y;

  @NotNull(message = "가시성은 필수입니다.") @Schema(description = "가시성 (COCO v 값)", example = "VISIBLE")
  private Visibility visibility;
}
