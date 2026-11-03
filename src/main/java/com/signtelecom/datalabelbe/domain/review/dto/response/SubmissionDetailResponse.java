/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.dto.response;

import java.util.List;

import com.signtelecom.datalabelbe.domain.label.dto.response.PoseLabelResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "SubmissionDetailResponse DTO", description = "검수 상세 화면용 제출 건 전체")
public class SubmissionDetailResponse {

  @Schema(description = "제출 건 요약")
  private SubmissionResponse submission;

  @Schema(description = "라벨 전체. 검수 대기 건은 현재 라벨을, 승인 건은 승인 시점에 고정된 스냅샷을 돌려준다. editable 은 항상 false")
  private PoseLabelResponse label;

  @Schema(description = "이 제출 건에 달린 코멘트 목록")
  private List<CommentResponse> comments;

  @Schema(description = "라벨의 출처. LIVE = 현재 라벨, SNAPSHOT = 승인 시점 고정본", example = "LIVE")
  private String labelSource;
}
