/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "CommentResponse DTO", description = "검수 코멘트")
public class CommentResponse {

  @Schema(description = "코멘트 식별자", example = "1")
  private Long commentId;

  @Schema(description = "이 코멘트가 달린 제출 식별자", example = "2")
  private Long submissionId;

  @Schema(description = "대상 인물의 표시 순서(0부터). null 이면 이미지 전체에 대한 코멘트", example = "0", nullable = true)
  private Integer targetObjectIndex;

  @Schema(description = "코멘트 내용", example = "왼쪽 손목 위치가 어긋났습니다.")
  private String text;

  @Schema(description = "작성자 식별자", example = "1")
  private Long authorId;

  @Schema(description = "작성자 이름", example = "윤희준")
  private String authorName;

  @Schema(description = "작성 일시", example = "2026-09-30T16:40:12")
  private LocalDateTime createdAt;
}
