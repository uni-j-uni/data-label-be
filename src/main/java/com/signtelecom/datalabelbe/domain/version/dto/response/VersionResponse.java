/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "VersionResponse DTO", description = "데이터셋 버전 요약")
public class VersionResponse {

  @Schema(description = "버전 식별자", example = "1")
  private Long versionId;

  @Schema(description = "버전 이름", example = "v1.0-초기검증")
  private String name;

  @Schema(description = "대상 배치 식별자", example = "1")
  private Long batchId;

  @Schema(description = "대상 배치 이름", example = "2026-09 보행자 포즈 1차")
  private String batchName;

  @Schema(description = "포함된 이미지 수", example = "6")
  private Integer imageCount;

  @Schema(description = "포함된 annotation(인물 객체) 수", example = "8")
  private Integer annotationCount;

  @Schema(description = "실제로 찍힌 키포인트 수", example = "132")
  private Integer labeledKeypointCount;

  @Schema(description = "객체당 키포인트 수 (COCO 고정값)", example = "17")
  private int keypointsPerObject;

  @Schema(description = "고정한 사용자 이름", example = "윤희준")
  private String createdByName;

  @Schema(description = "고정 일시", example = "2026-09-30T17:20:00")
  private LocalDateTime createdAt;
}
