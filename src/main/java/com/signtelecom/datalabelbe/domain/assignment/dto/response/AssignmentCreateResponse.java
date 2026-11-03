/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "AssignmentCreateResponse DTO", description = "작업 할당 결과 반환")
public class AssignmentCreateResponse {

  @Schema(description = "실제로 할당(신규 생성 + 담당자 교체)된 건수", example = "4")
  private int assignedCount;

  @Schema(description = "승인 완료 상태여서 건너뛴 건수", example = "1")
  private int skippedCount;

  @Schema(description = "할당된 이미지 목록. 요청에 넘긴 순서를 유지한다")
  private List<AssignmentResponse> assignments;

  @Schema(description = "승인 완료 상태여서 건너뛴 이미지 식별자 목록", example = "[5]")
  private List<Long> skippedImageIds;
}
