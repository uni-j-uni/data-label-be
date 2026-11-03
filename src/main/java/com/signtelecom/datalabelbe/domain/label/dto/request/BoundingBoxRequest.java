/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "BoundingBoxRequest DTO", description = "인물 바운딩 박스 (좌측 상단 기준 픽셀 좌표)")
public class BoundingBoxRequest {

  @NotNull(message = "x 좌표는 필수입니다.") @PositiveOrZero(message = "x 좌표는 0 이상이어야 합니다.")
  @Schema(description = "좌측 상단 x", example = "120.5")
  private Double x;

  @NotNull(message = "y 좌표는 필수입니다.") @PositiveOrZero(message = "y 좌표는 0 이상이어야 합니다.")
  @Schema(description = "좌측 상단 y", example = "80.0")
  private Double y;

  @NotNull(message = "너비는 필수입니다.") @Schema(description = "너비", example = "200.0")
  private Double width;

  @NotNull(message = "높이는 필수입니다.") @Schema(description = "높이", example = "450.0")
  private Double height;
}
