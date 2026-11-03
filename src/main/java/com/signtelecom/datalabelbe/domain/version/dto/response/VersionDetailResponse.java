/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "VersionDetailResponse DTO", description = "버전 통계와 포함 이미지 상세")
public class VersionDetailResponse {

  @Schema(description = "버전 요약과 통계")
  private VersionResponse version;

  @Schema(description = "포함된 이미지 상세 목록")
  private List<VersionImageResponse> images;
}
