/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.dto.request;

import jakarta.validation.constraints.NotNull;
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
@Schema(title = "VersionCreateRequest DTO", description = "현재 승인본으로 데이터셋 버전을 고정하기 위한 요청")
public class VersionCreateRequest {

  @NotNull(message = "배치 식별자는 필수입니다.") @Schema(description = "버전을 만들 배치 식별자", example = "1")
  private Long batchId;

  @Size(max = 100, message = "버전 이름은 100자 이내여야 합니다.")
  @Schema(description = "버전 이름. 비우면 v1, v2 ... 로 자동 생성", example = "v1.0-초기검증", nullable = true)
  private String name;
}
