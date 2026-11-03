/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.dto.response;

import java.util.Map;

import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(
    title = "MyAssignmentSummaryResponse DTO",
    description = "내 작업 상태별 건수와 이어서 작업할 이미지. 목록이 커서 단위라 화면 상단 숫자와 버튼은 이 응답으로 채운다")
public class MyAssignmentSummaryResponse {

  @Schema(description = "본인에게 할당된 전체 건수", example = "2693")
  private long totalCount;

  @Schema(
      description = "상태별 건수. 건이 없는 상태도 0으로 포함 (PENDING 은 할당이 없는 상태라 항상 0)",
      example =
          "{\"IN_PROGRESS\": 2500, \"PENDING_REVIEW\": 30, \"REJECTED\": 3, \"APPROVED\": 160}")
  private Map<AssignmentStatus, Long> statusCounts;

  @Schema(
      description = "이어서 작업할 이미지 식별자. 수정 가능(진행중·반려)한 할당 중 가장 최근 것. 없으면 null",
      example = "316",
      nullable = true)
  private Long nextImageId;
}
