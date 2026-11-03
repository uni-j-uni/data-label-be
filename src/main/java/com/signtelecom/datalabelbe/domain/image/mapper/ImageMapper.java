/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.signtelecom.datalabelbe.domain.assignment.entity.Assignment;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageBatchResponse;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageResponse;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadFailure;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadItem;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadResponse;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadStatus;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.image.entity.ImageBatch;
import com.signtelecom.datalabelbe.domain.image.service.BatchCounts;
import com.signtelecom.datalabelbe.domain.image.service.StoredImage;
import com.signtelecom.datalabelbe.domain.user.entity.User;

@Component
public class ImageMapper {

  /** 이미지 파일 조회 엔드포인트 경로. ImageController 의 매핑과 함께 관리해야 함 */
  private static final String IMAGE_FILE_PATH = "/api/image-batches/%d/images/%d/file";

  /** 썸네일 기본 너비 */
  private static final int DEFAULT_THUMBNAIL_WIDTH = 320;

  /**
   * 이미지 파일 조회 경로를 생성
   *
   * <p>할당 응답 등 다른 도메인에서도 같은 경로가 필요하므로, 경로 문자열이 여러 곳으로 복제되지 않도록 여기서만 만듦
   *
   * @param batchId 이미지가 속한 배치 식별자
   * @param imageId 이미지 식별자
   * @return 파일 조회 경로
   */
  public String toImageFileUrl(Long batchId, Long imageId) {
    return IMAGE_FILE_PATH.formatted(batchId, imageId);
  }

  /**
   * 썸네일 조회 경로를 생성
   *
   * <p>기본 너비를 붙인 경로 반환
   *
   * @param batchId 이미지가 속한 배치 식별자
   * @param imageId 이미지 식별자
   * @return 썸네일 조회 경로
   */
  public String toThumbnailUrl(Long batchId, Long imageId) {
    return "%s?w=%d".formatted(toImageFileUrl(batchId, imageId), DEFAULT_THUMBNAIL_WIDTH);
  }

  /**
   * 배치 이름과 생성자를 기반으로 ImageBatch 엔티티를 생성
   *
   * @param name 배치 이름 (호출부에서 자동 생성 여부를 이미 처리한 값)
   * @param createdBy 배치를 생성하는 사용자
   * @return 신규 생성된 ImageBatch 엔티티
   */
  public ImageBatch toImageBatch(String name, User createdBy) {
    return ImageBatch.builder().name(name).createdBy(createdBy).build();
  }

  /**
   * ImageBatch 엔티티를 배치 응답 DTO로 변환
   *
   * @param batch 배치 엔티티
   * @param counts 이미지 수와 할당·승인·검수대기 집계
   * @return 배치 응답 DTO
   */
  public ImageBatchResponse toImageBatchResponse(ImageBatch batch, BatchCounts counts) {
    return ImageBatchResponse.builder()
        .batchId(batch.getId())
        .name(batch.getName())
        .createdBy(batch.getCreatedBy().getId())
        .imageCount(counts.imageCount())
        .assignedCount(counts.assignedCount())
        .approvedCount(counts.approvedCount())
        .pendingReviewCount(counts.pendingReviewCount())
        .createdAt(batch.getCreatedAt())
        .updatedAt(batch.getUpdatedAt())
        .build();
  }

  /**
   * 파일별 처리 결과를 모아 업로드 응답 DTO로 변환
   *
   * @param batch 이미지가 저장된 배치
   * @param items 요청에 포함된 모든 파일의 처리 결과
   * @param failures 검증에 실패한 파일과 그 사유
   * @return 업로드 응답 DTO
   */
  public ImageUploadResponse toImageUploadResponse(
      ImageBatch batch, List<ImageUploadItem> items, List<ImageUploadFailure> failures) {
    return ImageUploadResponse.builder()
        .batchId(batch.getId())
        .batchName(batch.getName())
        .successCount(items.size() - failures.size())
        .failureCount(failures.size())
        .images(items)
        .failures(failures)
        .build();
  }

  /** 저장에 성공한 이미지의 처리 결과 항목을 생성 */
  public ImageUploadItem toSuccessItem(Image image) {
    return ImageUploadItem.builder()
        .id(image.getId())
        .filename(image.getOriginalFilename())
        .status(ImageUploadStatus.SUCCESS)
        .build();
  }

  /** 검증에 실패한 파일의 처리 결과 항목을 생성 */
  public ImageUploadItem toFailedItem(String filename) {
    return ImageUploadItem.builder().filename(filename).status(ImageUploadStatus.FAILED).build();
  }

  /** 검증에 실패한 파일의 사유 항목을 생성 */
  public ImageUploadFailure toFailure(String filename, String reason) {
    return ImageUploadFailure.builder().filename(filename).reason(reason).build();
  }

  /**
   * 저장이 끝난 파일의 메타데이터를 기반으로 Image 엔티티를 생성
   *
   * @param batch 이미지가 속할 배치
   * @param originalFilename 업로드 당시 원본 파일명
   * @param stored 저장소에 기록된 파일의 메타데이터
   * @return 신규 생성된 Image 엔티티
   */
  public Image toImage(ImageBatch batch, String originalFilename, StoredImage stored) {
    return Image.builder()
        .batch(batch)
        .originalFilename(originalFilename)
        .storagePath(stored.storagePath())
        .contentType(stored.contentType())
        .sizeBytes(stored.sizeBytes())
        .width(stored.width())
        .height(stored.height())
        .build();
  }

  /**
   * Image 엔티티와 그 이미지의 작업 할당을 합쳐 응답 DTO로 변환
   *
   * <p>할당이 없으면 status를 PENDING(대기)으로 채우고 담당자 정보는 null
   *
   * @param image 이미지 엔티티
   * @param assignment 그 이미지의 할당. 미할당이면 null
   * @param objectCount 라벨링된 인물 객체 수
   * @param labelSaved 라벨 저장 여부
   * @return 이미지 응답 DTO
   */
  public ImageResponse toImageResponse(
      Image image, Assignment assignment, long objectCount, boolean labelSaved) {
    ImageBatch batch = image.getBatch();
    Long batchId = batch.getId();

    // 할당이 없는 이미지는 PENDING(대기)으로 응답
    ImageResponse.ImageResponseBuilder builder =
        ImageResponse.builder()
            .imageId(image.getId())
            .batchId(batchId)
            .batchName(batch.getName())
            .originalFilename(image.getOriginalFilename())
            .url(toImageFileUrl(batchId, image.getId()))
            .thumbnailUrl(toThumbnailUrl(batchId, image.getId()))
            .storagePath(image.getStoragePath())
            .contentType(image.getContentType())
            .sizeBytes(image.getSizeBytes())
            .width(image.getWidth())
            .height(image.getHeight())
            .createdAt(image.getCreatedAt())
            .status(AssignmentStatus.PENDING)
            .objectCount(objectCount)
            .labelSaved(labelSaved);

    if (assignment != null) {
      builder
          .status(assignment.getStatus())
          .assignmentId(assignment.getId())
          .workerId(assignment.getWorker().getId())
          .workerName(assignment.getWorker().getName());
    }

    return builder.build();
  }
}
