/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 제출 건의 검수 상태. 작업자의 할당 상태와 달리, 제출 1건마다 독립적으로 남는 이력 */
@Getter
@RequiredArgsConstructor
@Schema(description = "제출 검수 상태 Enum")
public enum SubmissionStatus {
  @Schema(description = "검수를 기다리는 중")
  PENDING_REVIEW("검수대기"),
  @Schema(description = "승인됨. 이 시점의 라벨이 스냅샷으로 고정된다")
  APPROVED("승인"),
  @Schema(description = "반려됨. 담당 작업자가 재작업한다")
  REJECTED("반려");

  private final String ko;
}
