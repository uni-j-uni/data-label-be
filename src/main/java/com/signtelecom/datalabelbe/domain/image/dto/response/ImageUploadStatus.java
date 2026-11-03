/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "파일별 업로드 처리 결과 Enum")
public enum ImageUploadStatus {
  @Schema(description = "저장 성공")
  SUCCESS("성공"),
  @Schema(description = "검증 실패로 저장되지 않음")
  FAILED("실패");

  private final String ko;
}
