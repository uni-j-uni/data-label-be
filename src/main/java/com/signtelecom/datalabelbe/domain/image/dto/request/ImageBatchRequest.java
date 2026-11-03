/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.dto.request;

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
@Schema(title = "ImageBatchRequest DTO", description = "이미지 배치 생성 및 수정을 위한 데이터 전송")
public class ImageBatchRequest {

  @NotBlank(message = "배치 이름 항목은 필수입니다.")
  @Size(min = 1, max = 100, message = "배치 이름은 1~100자여야 합니다.")
  @Schema(description = "배치 이름", example = "2026-09 도로 표지판 1차", minLength = 1, maxLength = 100)
  private String name;
}
