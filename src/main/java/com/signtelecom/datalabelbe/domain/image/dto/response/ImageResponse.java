/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.dto.response;

import java.time.LocalDateTime;

import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(
    title = "ImageResponse DTO",
    description = "이미지 정보와 작업 할당 현황을 합친 응답. 미할당 이미지는 status=PENDING, 할당 관련 필드는 null")
public class ImageResponse {

  @Schema(description = "이미지 식별자", example = "1")
  private Long imageId;

  @Schema(description = "이미지가 속한 배치 식별자", example = "1")
  private Long batchId;

  @Schema(description = "업로드 당시 원본 파일명", example = "sign_0001.jpg")
  private String originalFilename;

  @Schema(
      description = "이미지 파일 조회 경로. 서버 호스트를 앞에 붙여 <img> src 로 바로 사용할 수 있다",
      example = "/api/image-batches/1/images/1/file")
  private String url;

  @Schema(
      description = "썸네일 조회 경로(너비 320px, JPEG). 그리드에서 원본 대신 사용한다",
      example = "/api/image-batches/1/images/1/file?w=320")
  private String thumbnailUrl;

  @Schema(
      description = "저장소 내 파일 경로 (UUID 기반)",
      example = "batch/1/9f1c2d3e-4b5a-6789-abcd-ef0123456789.jpg")
  private String storagePath;

  @Schema(description = "MIME 타입", example = "image/jpeg")
  private String contentType;

  @Schema(description = "파일 크기 (byte)", example = "204800")
  private Long sizeBytes;

  @Schema(description = "이미지 너비 (px)", example = "1920")
  private Integer width;

  @Schema(description = "이미지 높이 (px)", example = "1080")
  private Integer height;

  @Schema(description = "생성 일시", example = "2026-09-30T14:20:11")
  private LocalDateTime createdAt;

  @Schema(description = "이미지가 속한 배치 이름", example = "2026-09 보행자 포즈 1차")
  private String batchName;

  @Schema(description = "작업 상태. 할당이 없으면 PENDING(대기)으로 내려간다", example = "IN_PROGRESS")
  private AssignmentStatus status;

  @Schema(description = "할당 식별자. 미할당이면 null", example = "1", nullable = true)
  private Long assignmentId;

  @Schema(description = "담당 작업자 식별자. 미할당이면 null", example = "2", nullable = true)
  private Long workerId;

  @Schema(description = "담당 작업자 이름. 미할당이면 null", example = "김사원", nullable = true)
  private String workerName;

  @Schema(description = "라벨링된 인물 객체 수", example = "2")
  private long objectCount;

  @Schema(description = "라벨 저장 여부. 객체 0개로 저장한 경우도 true", example = "true")
  private boolean labelSaved;
}
