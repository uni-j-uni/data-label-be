/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 작업 할당 상태
 *
 * <p>PENDING(대기)은 할당이 없는 이미지를 표현할 때만 쓰고 DB에는 저장하지 않음. 할당은 IN_PROGRESS로 생성되므로 대기 건수는 (이미지 수 - 할당 수)로
 * 계산
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "작업 할당 상태 Enum")
public enum AssignmentStatus {
  @Schema(description = "미할당을 뜻하는 논리 값. 할당 레코드에는 저장되지 않는다")
  PENDING("대기"),
  @Schema(description = "작업자가 라벨링을 진행 중")
  IN_PROGRESS("진행중"),
  @Schema(description = "작업자가 제출해 검수를 기다림")
  PENDING_REVIEW("검수대기"),
  @Schema(description = "검수에서 반려됨")
  REJECTED("반려"),
  @Schema(description = "검수에서 승인됨")
  APPROVED("승인");

  private final String ko;
}
