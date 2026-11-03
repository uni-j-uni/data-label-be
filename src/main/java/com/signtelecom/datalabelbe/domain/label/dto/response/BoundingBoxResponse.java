/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "BoundingBoxResponse DTO", description = "인물 바운딩 박스")
public class BoundingBoxResponse {

  @Schema(description = "좌측 상단 x", example = "120.5")
  private Double x;

  @Schema(description = "좌측 상단 y", example = "80.0")
  private Double y;

  @Schema(description = "너비", example = "200.0")
  private Double width;

  @Schema(description = "높이", example = "450.0")
  private Double height;
}
