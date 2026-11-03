/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "CommentRequest DTO", description = "검수 코멘트 하나")
public class CommentRequest {

  @Schema(description = "대상 인물 객체 식별자. 비우면 이미지 전체에 대한 코멘트", example = "3", nullable = true)
  private Long targetObjectId;

  @NotBlank(message = "코멘트 내용은 필수입니다.")
  @Size(max = 1000, message = "코멘트는 1000자 이내여야 합니다.")
  @Schema(description = "코멘트 내용", example = "왼쪽 손목 위치가 어긋났습니다.")
  private String text;
}
