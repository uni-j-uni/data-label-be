/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.signtelecom.datalabelbe.domain.review.entity.ReviewDecision;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ReviewRequest DTO", description = "제출 건에 대한 승인·반려 처리 요청")
public class ReviewRequest {

  @NotNull(message = "제출 식별자는 필수입니다.") @Schema(description = "검수할 제출 식별자", example = "1")
  private Long submissionId;

  @NotNull(message = "판정은 필수입니다.") @Schema(description = "승인 또는 반려", example = "REJECT")
  private ReviewDecision decision;

  @Valid
  @Schema(description = "코멘트 목록. 반려 시에는 최소 1건이 필요하다. 승인 시에는 생략 가능")
  private List<CommentRequest> comments;
}
