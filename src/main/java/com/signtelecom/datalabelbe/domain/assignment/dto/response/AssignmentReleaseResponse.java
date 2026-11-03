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
@Schema(title = "AssignmentReleaseResponse DTO", description = "작업 할당 해제 결과 반환")
public class AssignmentReleaseResponse {

  @Schema(description = "실제로 해제된 건수", example = "2")
  private int releasedCount;

  @Schema(description = "해제된 이미지 식별자 목록. 이 이미지들은 다시 미할당(대기) 상태가 된다", example = "[1, 2]")
  private List<Long> releasedImageIds;

  @Schema(description = "해제할 수 없어 건너뛴 건수 (라벨 저장됨·반려·검수대기·승인)", example = "1")
  private int skippedCount;

  @Schema(description = "건너뛴 이미지 식별자 목록. 미할당 이미지는 포함하지 않음", example = "[3]")
  private List<Long> skippedImageIds;
}
