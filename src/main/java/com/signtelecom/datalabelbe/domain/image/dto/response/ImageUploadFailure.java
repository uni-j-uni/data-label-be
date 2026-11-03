/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "ImageUploadFailure DTO", description = "검증에 실패해 저장되지 않은 파일의 사유")
public class ImageUploadFailure {

  @Schema(description = "업로드 당시 원본 파일명", example = "broken.gif")
  private String filename;

  @Schema(description = "실패 사유", example = "JPEG 또는 PNG 이미지만 업로드할 수 있습니다.")
  private String reason;
}
