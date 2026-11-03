/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "VersionImageResponse DTO", description = "버전에 포함된 이미지 한 건")
public class VersionImageResponse {

  @Schema(description = "이미지 식별자", example = "1")
  private Long imageId;

  @Schema(description = "원본 파일명", example = "sign_0001.jpg")
  private String filename;

  @Schema(description = "너비 (px)", example = "1920")
  private int width;

  @Schema(description = "높이 (px)", example = "1080")
  private int height;

  @Schema(description = "인물 객체 수", example = "2")
  private int objectCount;

  @Schema(description = "실제로 찍힌 키포인트 수", example = "34")
  private int labeledKeypointCount;

  @Schema(description = "채워야 할 전체 키포인트 수 (객체 수 x 17). 진행바 분모로 쓴다", example = "34")
  private int totalKeypointCount;
}
