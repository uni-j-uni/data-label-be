/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "ImageUploadItem DTO", description = "업로드 요청에 포함된 파일 하나의 처리 결과")
public class ImageUploadItem {

  @Schema(description = "저장된 이미지 식별자. 실패한 파일은 null", example = "1")
  private Long id;

  @Schema(description = "업로드 당시 원본 파일명", example = "sign_0001.jpg")
  private String filename;

  @Schema(description = "처리 결과", example = "SUCCESS")
  private ImageUploadStatus status;
}
