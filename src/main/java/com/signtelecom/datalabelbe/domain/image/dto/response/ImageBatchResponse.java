/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "ImageBatchResponse DTO", description = "이미지 배치 정보에 대한 응답 반환")
public class ImageBatchResponse {

  @Schema(description = "배치 식별자", example = "1")
  private Long batchId;

  @Schema(description = "배치 이름", example = "2026-09 도로 표지판 1차")
  private String name;

  @Schema(description = "배치를 생성한 사용자 식별자", example = "1")
  private Long createdBy;

  @Schema(description = "배치에 포함된 이미지 수", example = "24")
  private Long imageCount;

  @Schema(description = "작업자에게 할당된 이미지 수", example = "20")
  private Long assignedCount;

  @Schema(description = "검수에서 승인된 이미지 수", example = "12")
  private Long approvedCount;

  @Schema(description = "검수를 기다리는 이미지 수", example = "8")
  private Long pendingReviewCount;

  @Schema(description = "생성 일시", example = "2026-09-30T14:20:11")
  private LocalDateTime createdAt;

  @Schema(description = "수정 일시", example = "2026-09-30T14:20:11")
  private LocalDateTime updatedAt;
}
