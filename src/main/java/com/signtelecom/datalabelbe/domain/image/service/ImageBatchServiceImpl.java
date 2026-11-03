/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentHistoryRepository;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentRepository;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentRepository.BatchStatusCount;
import com.signtelecom.datalabelbe.domain.image.dto.request.ImageBatchRequest;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageBatchResponse;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.image.entity.ImageBatch;
import com.signtelecom.datalabelbe.domain.image.exception.ImageErrorCode;
import com.signtelecom.datalabelbe.domain.image.mapper.ImageMapper;
import com.signtelecom.datalabelbe.domain.image.repository.ImageBatchRepository;
import com.signtelecom.datalabelbe.domain.image.repository.ImageRepository;
import com.signtelecom.datalabelbe.domain.image.repository.ImageRepository.BatchImageCount;
import com.signtelecom.datalabelbe.domain.label.entity.PoseLabel;
import com.signtelecom.datalabelbe.domain.label.repository.PoseLabelRepository;
import com.signtelecom.datalabelbe.domain.review.entity.Submission;
import com.signtelecom.datalabelbe.domain.review.repository.SubmissionRepository;
import com.signtelecom.datalabelbe.domain.review.repository.SubmissionSnapshotRepository;
import com.signtelecom.datalabelbe.domain.user.entity.Role;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.domain.user.service.UserService;
import com.signtelecom.datalabelbe.domain.version.entity.DatasetVersion;
import com.signtelecom.datalabelbe.domain.version.repository.DatasetVersionRepository;
import com.signtelecom.datalabelbe.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageBatchServiceImpl implements ImageBatchService {

  /** 이름을 비우고 올린 배치의 자동 이름 형식. 같은 날 여러 번 올려도 구분되도록 시·분까지 씀 */
  private static final DateTimeFormatter AUTO_NAME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final ImageBatchRepository imageBatchRepository;
  private final ImageRepository imageRepository;
  private final ImageStorageService imageStorageService;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentHistoryRepository assignmentHistoryRepository;
  private final PoseLabelRepository poseLabelRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionSnapshotRepository submissionSnapshotRepository;
  private final DatasetVersionRepository datasetVersionRepository;
  private final ImageMapper imageMapper;
  private final UserService userService;

  @Override
  @Transactional
  public ImageBatch createBatch(String name) {

    User currentUser = userService.getCurrentUser();
    ImageBatch savedBatch =
        imageBatchRepository.save(imageMapper.toImageBatch(resolveName(name), currentUser));

    log.info(
        "이미지 배치 생성 - batchId: {}, name: {}, userId: {}",
        savedBatch.getId(),
        savedBatch.getName(),
        currentUser.getId());

    return savedBatch;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ImageBatchResponse> getBatches() {

    List<ImageBatch> batches = imageBatchRepository.findAllByOrderByCreatedAtDesc();

    // 배치마다 count 쿼리를 던지지 않도록 이미지 수와 할당 상태별 건수를 각각 한 번에 집계
    Map<Long, Long> imageCounts =
        imageRepository.countGroupedByBatchId().stream()
            .collect(Collectors.toMap(BatchImageCount::getBatchId, BatchImageCount::getImageCount));

    Map<Long, Map<AssignmentStatus, Long>> assignmentCounts =
        assignmentRepository.countGroupedByBatchIdAndStatus().stream()
            .collect(
                Collectors.groupingBy(
                    BatchStatusCount::getBatchId,
                    Collectors.toMap(BatchStatusCount::getStatus, BatchStatusCount::getCount)));

    log.info("이미지 배치 목록 조회 - 배치 수: {}", batches.size());

    return batches.stream()
        .map(
            batch ->
                imageMapper.toImageBatchResponse(
                    batch,
                    toBatchCounts(
                        imageCounts.getOrDefault(batch.getId(), 0L),
                        assignmentCounts.getOrDefault(batch.getId(), Map.of()))))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ImageBatchResponse getBatch(Long batchId) {

    ImageBatch batch = getOwnedBatch(batchId);

    log.info("이미지 배치 상세 조회 - batchId: {}", batchId);

    return imageMapper.toImageBatchResponse(batch, countsOf(batchId));
  }

  @Override
  @Transactional
  public ImageBatchResponse updateBatch(Long batchId, ImageBatchRequest request) {

    ImageBatch batch = getOwnedBatch(batchId);
    batch.rename(request.getName());

    log.info("이미지 배치 이름 변경 - batchId: {}, name: {}", batchId, request.getName());

    return imageMapper.toImageBatchResponse(batch, countsOf(batchId));
  }

  @Override
  @Transactional
  public void deleteBatch(Long batchId) {

    ImageBatch batch = getOwnedBatch(batchId);
    List<Image> images = imageRepository.findByBatchId(batchId);

    // 할당·할당 이력·라벨·제출이 이미지를 참조하므로 이미지보다 먼저 지워야 FK 제약에 걸리지 않음
    // 라벨과 제출은 벌크 delete 대신 엔티티로 지움. 자식(PoseObject/Keypoint, Comment)이
    // CASCADE 로 함께 정리되어야 하기 때문. 스냅샷은 제출과 JPA 연관이 없으므로 제출보다 먼저 벌크로 지움
    List<DatasetVersion> versions = datasetVersionRepository.findByBatchId(batchId);
    datasetVersionRepository.deleteAll(versions);
    submissionSnapshotRepository.deleteByBatchId(batchId);
    List<Submission> submissions = submissionRepository.findByBatchId(batchId);
    submissionRepository.deleteAll(submissions);
    int deletedAssignments = assignmentRepository.deleteByBatchId(batchId);
    assignmentHistoryRepository.deleteByBatchId(batchId);
    List<PoseLabel> labels = poseLabelRepository.findByBatchId(batchId);
    poseLabelRepository.deleteAll(labels);
    imageRepository.deleteAll(images);
    imageBatchRepository.delete(batch);

    // 파일 삭제는 롤백되지 않음. 커밋 이후 이 지점 뒤에서 예외가 발생하면 DB 레코드만 남을 수 있으므로,
    // 실패를 감지할 수 있도록 deleteBatchDirectory()는 예외를 던지지 않고 경고 로그만 남김
    imageStorageService.deleteBatchDirectory(batchId);

    log.info(
        "이미지 배치 삭제 - batchId: {}, 이미지: {}건, 할당: {}건, 라벨: {}건, 제출: {}건, 버전: {}건",
        batchId,
        images.size(),
        deletedAssignments,
        labels.size(),
        submissions.size(),
        versions.size());
  }

  @Override
  @Transactional(readOnly = true)
  public ImageBatch getOwnedBatch(Long batchId) {

    User currentUser = userService.getCurrentUser();
    ImageBatch batch =
        imageBatchRepository
            .findById(batchId)
            .orElseThrow(
                () -> {
                  log.info("이미지 배치 찾기 실패 - batchId: {}", batchId);
                  return new CustomException(ImageErrorCode.IMAGE_BATCH_NOT_FOUND);
                });

    // 목록 조회가 ADMIN 전용 전체 조회이므로, ADMIN은 자신이 만들지 않은 배치도 열 수 있어야 함
    if (!batch.isOwnedBy(currentUser) && currentUser.getRole() != Role.ROLE_ADMIN) {
      log.warn("이미지 배치 접근 권한 없음 - batchId: {}, userId: {}", batchId, currentUser.getId());
      throw new CustomException(ImageErrorCode.IMAGE_BATCH_FORBIDDEN);
    }

    return batch;
  }

  /**
   * 이름이 비어 있으면 현재 날짜와 시·분으로 배치 이름을 만듦
   *
   * <p>날짜까지만 쓰면 같은 날 올린 배치가 모두 같은 이름이 되어 목록에서 구분할 수 없음. 시·분까지 넣어 업로드 시점으로 구별
   */
  private String resolveName(String name) {
    if (name != null && !name.isBlank()) {
      return name.trim();
    }
    return "%s 배치".formatted(LocalDateTime.now().format(AUTO_NAME_FORMAT));
  }

  /** 배치 하나의 이미지·할당 집계를 계산 */
  private BatchCounts countsOf(Long batchId) {
    return new BatchCounts(
        imageRepository.countByBatchId(batchId),
        assignmentRepository.countByBatchId(batchId),
        assignmentRepository.countByBatchIdAndStatus(batchId, AssignmentStatus.APPROVED),
        assignmentRepository.countByBatchIdAndStatus(batchId, AssignmentStatus.PENDING_REVIEW));
  }

  /** 집계 쿼리 결과를 배치 하나의 집계 값으로 변환 */
  private BatchCounts toBatchCounts(long imageCount, Map<AssignmentStatus, Long> byStatus) {
    long assigned = byStatus.values().stream().mapToLong(Long::longValue).sum();

    return new BatchCounts(
        imageCount,
        assigned,
        byStatus.getOrDefault(AssignmentStatus.APPROVED, 0L),
        byStatus.getOrDefault(AssignmentStatus.PENDING_REVIEW, 0L));
  }
}
