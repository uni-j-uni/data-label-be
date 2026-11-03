/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.dto.response;

import java.util.Map;

import com.signtelecom.datalabelbe.domain.review.entity.SubmissionStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(
    title = "SubmissionSummaryResponse DTO",
    description = "검수 상태별 제출 건수. 목록이 페이지 단위라 탭 배지 숫자는 이 응답으로 채운다")
public class SubmissionSummaryResponse {

  @Schema(description = "전체 제출 건수", example = "141")
  private long totalCount;

  @Schema(
      description = "상태별 제출 건수. 건이 없는 상태도 0으로 포함",
      example = "{\"PENDING_REVIEW\": 12, \"APPROVED\": 120, \"REJECTED\": 9}")
  private Map<SubmissionStatus, Long> statusCounts;
}
