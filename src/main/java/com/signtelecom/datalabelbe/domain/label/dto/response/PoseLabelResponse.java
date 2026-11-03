/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "PoseLabelResponse DTO", description = "이미지 한 장의 포즈 라벨 전체")
public class PoseLabelResponse {

  @Schema(description = "라벨 식별자. 아직 저장된 적이 없으면 null", example = "1", nullable = true)
  private Long labelId;

  @Schema(description = "이미지 식별자", example = "1")
  private Long imageId;

  @Schema(description = "이미지가 속한 배치 식별자", example = "26")
  private Long batchId;

  @Schema(
      description = "작업 할당 식별자. 할당이 없으면 null. 검수 요청(submit) 경로에 사용",
      example = "39",
      nullable = true)
  private Long assignmentId;

  @Schema(description = "이미지 너비 (px). 좌표 검증 기준", example = "1920")
  private Integer imageWidth;

  @Schema(description = "이미지 높이 (px). 좌표 검증 기준", example = "1080")
  private Integer imageHeight;

  @Schema(description = "작업 상태. 할당이 없으면 PENDING", example = "IN_PROGRESS")
  private AssignmentStatus status;

  @Schema(description = "수정 가능 여부. 검수 대기·승인 완료 상태이면 false", example = "true")
  private boolean editable;

  @Schema(description = "인물 객체 목록")
  private List<PoseObjectResponse> objects;

  @Schema(description = "최초 저장 일시", example = "2026-09-30T14:20:11", nullable = true)
  private LocalDateTime createdAt;

  @Schema(description = "마지막 저장 일시", example = "2026-09-30T15:02:44", nullable = true)
  private LocalDateTime updatedAt;
}
