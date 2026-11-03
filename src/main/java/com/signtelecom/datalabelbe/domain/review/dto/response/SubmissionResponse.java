/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.dto.response;

import java.time.LocalDateTime;

import com.signtelecom.datalabelbe.domain.review.entity.SubmissionStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "SubmissionResponse DTO", description = "검수 목록에 표시할 제출 건 요약")
public class SubmissionResponse {

  @Schema(description = "제출 식별자", example = "1")
  private Long submissionId;

  @Schema(description = "대상 이미지 식별자", example = "1")
  private Long imageId;

  @Schema(description = "이미지가 속한 배치 식별자", example = "1")
  private Long batchId;

  @Schema(description = "이미지가 속한 배치 이름", example = "2026-09 보행자 포즈 1차")
  private String batchName;

  @Schema(description = "이미지 원본 파일명", example = "sign_0001.jpg")
  private String filename;

  @Schema(
      description = "이미지 파일 조회 경로. 썸네일에 바로 쓸 수 있다",
      example = "/api/image-batches/1/images/1/file")
  private String url;

  @Schema(
      description = "썸네일 조회 경로(너비 320px, JPEG). 그리드에서 원본 대신 사용한다",
      example = "/api/image-batches/1/images/1/file?w=320")
  private String thumbnailUrl;

  @Schema(description = "제출자 식별자", example = "2")
  private Long submittedById;

  @Schema(description = "제출자 이름", example = "김사원")
  private String submittedByName;

  @Schema(description = "제출 시각", example = "2026-09-30T16:10:00")
  private LocalDateTime submittedAt;

  @Schema(description = "검수 상태", example = "PENDING_REVIEW")
  private SubmissionStatus status;

  @Schema(description = "제출 당시 인물 객체 수", example = "2")
  private Integer objectCount;

  @Schema(description = "제출 당시 찍힌 키포인트 수", example = "34")
  private Integer labeledKeypointCount;

  @Schema(description = "이 제출 건에 달린 코멘트 수", example = "2")
  private long commentCount;

  @Schema(description = "검수자 이름. 검수 전이면 null", example = "윤희준", nullable = true)
  private String reviewedByName;

  @Schema(description = "검수 시각. 검수 전이면 null", example = "2026-09-30T16:40:12", nullable = true)
  private LocalDateTime reviewedAt;
}
