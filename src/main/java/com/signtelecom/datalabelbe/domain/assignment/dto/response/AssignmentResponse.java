/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.dto.response;

import java.time.LocalDateTime;

import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "AssignmentResponse DTO", description = "작업 할당 정보에 대한 응답 반환")
public class AssignmentResponse {

  @Schema(description = "할당 식별자", example = "1")
  private Long assignmentId;

  @Schema(description = "할당된 이미지 식별자", example = "1")
  private Long imageId;

  @Schema(description = "이미지가 속한 배치 식별자", example = "1")
  private Long batchId;

  @Schema(description = "이미지가 속한 배치 이름", example = "2026-09 보행자 포즈 1차")
  private String batchName;

  @Schema(description = "이미지 원본 파일명", example = "sign_0001.jpg")
  private String filename;

  @Schema(
      description = "이미지 파일 조회 경로. 서버 호스트를 앞에 붙여 <img> src 로 바로 사용할 수 있다",
      example = "/api/image-batches/1/images/1/file")
  private String url;

  @Schema(
      description = "썸네일 조회 경로(너비 320px, JPEG). 그리드에서 원본 대신 사용한다",
      example = "/api/image-batches/1/images/1/file?w=320")
  private String thumbnailUrl;

  @Schema(description = "이미지 너비 (px)", example = "1920")
  private Integer width;

  @Schema(description = "이미지 높이 (px)", example = "1080")
  private Integer height;

  @Schema(description = "담당 작업자 식별자", example = "2")
  private Long workerId;

  @Schema(description = "담당 작업자 이름", example = "사원1")
  private String workerName;

  @Schema(description = "최초 배정한 관리자 식별자", example = "1")
  private Long assignedById;

  @Schema(description = "최초 배정한 관리자 이름", example = "관리자")
  private String assignedByName;

  @Schema(description = "할당 상태", example = "PENDING")
  private AssignmentStatus status;

  @Schema(description = "라벨링된 인물 객체 수", example = "2")
  private long objectCount;

  @Schema(description = "라벨 저장 여부. 객체 0개로 저장한 경우도 true", example = "true")
  private boolean labelSaved;

  @Schema(description = "할당 일시", example = "2026-09-30T14:20:11")
  private LocalDateTime createdAt;

  @Schema(description = "상태 변경 일시", example = "2026-09-30T15:02:44")
  private LocalDateTime updatedAt;
}
