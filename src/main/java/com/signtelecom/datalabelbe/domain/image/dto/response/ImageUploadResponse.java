/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "ImageUploadResponse DTO", description = "배치 업로드 결과 반환")
public class ImageUploadResponse {

  @Schema(description = "이미지가 저장된 배치 식별자", example = "1")
  private Long batchId;

  @Schema(description = "배치 이름", example = "2026-10-01 14:30 배치")
  private String batchName;

  @Schema(description = "저장 성공한 파일 수", example = "9")
  private int successCount;

  @Schema(description = "검증 실패한 파일 수", example = "1")
  private int failureCount;

  @Schema(description = "요청에 포함된 모든 파일의 처리 결과")
  private List<ImageUploadItem> images;

  @Schema(description = "실패한 파일과 사유. 성공만 있으면 빈 배열")
  private List<ImageUploadFailure> failures;
}
