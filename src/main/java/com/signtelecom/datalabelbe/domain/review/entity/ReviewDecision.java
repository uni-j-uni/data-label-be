/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "검수 판정 Enum")
public enum ReviewDecision {
  @Schema(description = "승인")
  APPROVE("승인"),
  @Schema(description = "반려")
  REJECT("반려");

  private final String ko;
}
