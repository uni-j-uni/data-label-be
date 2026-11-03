/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.image.entity.ImageBatch;
import com.signtelecom.datalabelbe.domain.image.service.ImageBatchService;
import com.signtelecom.datalabelbe.domain.review.entity.SubmissionSnapshot;
import com.signtelecom.datalabelbe.domain.review.repository.SubmissionSnapshotRepository;
import com.signtelecom.datalabelbe.domain.review.service.SnapshotPayload;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.domain.user.service.UserService;
import com.signtelecom.datalabelbe.domain.version.dto.request.VersionCreateRequest;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionDetailResponse;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionResponse;
import com.signtelecom.datalabelbe.domain.version.entity.DatasetVersion;
import com.signtelecom.datalabelbe.domain.version.exception.VersionErrorCode;
import com.signtelecom.datalabelbe.domain.version.mapper.VersionMapper;
import com.signtelecom.datalabelbe.domain.version.repository.DatasetVersionRepository;
import com.signtelecom.datalabelbe.global.exception.CustomException;
import com.signtelecom.datalabelbe.global.exception.ErrorGuard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

  private final DatasetVersionRepository datasetVersionRepository;
  private final SubmissionSnapshotRepository submissionSnapshotRepository;
  private final ImageBatchService imageBatchService;
  private final VersionMapper versionMapper;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public VersionResponse createVersion(VersionCreateRequest request) {

    Long batchId = request.getBatchId();
    ImageBatch batch = imageBatchService.getOwnedBatch(batchId);
    User currentUser = userService.getCurrentUser();

    // 이미지당 승인 건은 하나지만, 혹시 여러 건이면 최신 것만 씀
    Map<Long, SubmissionSnapshot> latestByImage = new LinkedHashMap<>();
    for (SubmissionSnapshot snapshot :
        submissionSnapshotRepository.findApprovedByBatchId(batchId)) {
      latestByImage.putIfAbsent(snapshot.getSubmission().getImage().getId(), snapshot);
    }

    if (latestByImage.isEmpty()) {
      log.info("승인된 라벨 없음 - batchId: {}", batchId);
      ErrorGuard.fail(VersionErrorCode.NO_APPROVED_LABEL);
    }

    String name = resolveName(request.getName(), batchId);
    LocalDateTime now = LocalDateTime.now();

    CocoDataset dataset = buildCoco(latestByImage.values(), name, now);

    int annotationCount = dataset.annotations().size();
    int labeledKeypointCount =
        dataset.annotations().stream().mapToInt(CocoDataset.CocoAnnotation::numKeypoints).sum();

    DatasetVersion version =
        datasetVersionRepository.save(
            DatasetVersion.builder()
                .name(name)
                .batch(batch)
                .createdBy(currentUser)
                .imageCount(dataset.images().size())
                .annotationCount(annotationCount)
                .labeledKeypointCount(labeledKeypointCount)
                .payload(writeJson(dataset))
                .build());

    log.info(
        "데이터셋 버전 고정 - versionId: {}, name: {}, 이미지: {}건, annotation: {}건, 키포인트: {}개",
        version.getId(),
        name,
        dataset.images().size(),
        annotationCount,
        labeledKeypointCount);

    return versionMapper.toVersionResponse(version);
  }

  @Override
  @Transactional(readOnly = true)
  public List<VersionResponse> getVersions() {

    List<DatasetVersion> versions = datasetVersionRepository.findAllWithBatchAndCreator();

    log.info("데이터셋 버전 목록 조회 - 버전 수: {}", versions.size());

    return versions.stream().map(versionMapper::toVersionResponse).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public VersionDetailResponse getVersion(Long versionId) {

    DatasetVersion version = findVersion(versionId);
    CocoDataset dataset = readJson(version);

    log.info("데이터셋 버전 상세 조회 - versionId: {}", versionId);

    return VersionDetailResponse.builder()
        .version(versionMapper.toVersionResponse(version))
        .images(versionMapper.toVersionImages(dataset))
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public String exportCoco(Long versionId) {

    DatasetVersion version = findVersion(versionId);

    log.info("COCO export - versionId: {}, name: {}", versionId, version.getName());

    return version.getPayload();
  }

  @Override
  @Transactional
  public void deleteVersion(Long versionId) {

    DatasetVersion version = findVersion(versionId);

    // 버전은 복사본이라 지워도 원본 라벨과 승인 스냅샷은 그대로 남음
    datasetVersionRepository.delete(version);

    log.info("데이터셋 버전 삭제 - versionId: {}, name: {}", versionId, version.getName());
  }

  @Override
  @Transactional(readOnly = true)
  public String exportFilename(Long versionId) {

    DatasetVersion version = findVersion(versionId);

    // 파일명에 쓸 수 없는 문자를 밑줄로 바꿈
    String batchName = version.getBatch().getName().replaceAll("[^\\p{L}\\p{N}._-]", "_");
    String versionName = version.getName().replaceAll("[^\\p{L}\\p{N}._-]", "_");

    return "%s_%s.json".formatted(batchName, versionName);
  }

  /** 승인 스냅샷들을 모아 COCO 구조로 조립 */
  private CocoDataset buildCoco(
      Iterable<SubmissionSnapshot> snapshots, String versionName, LocalDateTime createdAt) {

    List<CocoDataset.CocoImage> images = new ArrayList<>();
    List<CocoDataset.CocoAnnotation> annotations = new ArrayList<>();
    long annotationId = 1;

    for (SubmissionSnapshot snapshot : snapshots) {
      Image image = snapshot.getSubmission().getImage();

      // 해상도도 현재 이미지 정보가 아니라 스냅샷에 저장된 승인 시점 값을 사용
      images.add(
          versionMapper.toCocoImage(
              image.getId(),
              image.getOriginalFilename(),
              snapshot.getImageWidth(),
              snapshot.getImageHeight()));

      for (SnapshotPayload.SnapshotObject object : readPayload(snapshot).objects()) {
        annotations.add(versionMapper.toCocoAnnotation(annotationId++, image.getId(), object));
      }
    }

    return new CocoDataset(
        versionMapper.toCocoInfo(versionName, createdAt),
        images,
        annotations,
        List.of(versionMapper.personCategory()));
  }

  /** 이름이 비어 있으면 배치별 일련번호로 만듦 */
  private String resolveName(String name, Long batchId) {
    if (name != null && !name.isBlank()) {
      return name.trim();
    }
    return "v" + (datasetVersionRepository.countByBatchId(batchId) + 1);
  }

  private String writeJson(CocoDataset dataset) {
    try {
      return objectMapper.writeValueAsString(dataset);
    } catch (JsonProcessingException e) {
      log.error("COCO 직렬화 실패", e);
      return ErrorGuard.fail(VersionErrorCode.COCO_SERIALIZE_FAILED, e);
    }
  }

  private CocoDataset readJson(DatasetVersion version) {
    try {
      return objectMapper.readValue(version.getPayload(), CocoDataset.class);
    } catch (JsonProcessingException e) {
      log.error("COCO 역직렬화 실패 - versionId: {}", version.getId(), e);
      return ErrorGuard.fail(VersionErrorCode.COCO_SERIALIZE_FAILED, e);
    }
  }

  private SnapshotPayload readPayload(SubmissionSnapshot snapshot) {
    try {
      return objectMapper.readValue(snapshot.getPayload(), SnapshotPayload.class);
    } catch (JsonProcessingException e) {
      log.error("스냅샷 역직렬화 실패 - snapshotId: {}", snapshot.getId(), e);
      return ErrorGuard.fail(VersionErrorCode.COCO_SERIALIZE_FAILED, e);
    }
  }

  private DatasetVersion findVersion(Long versionId) {
    return datasetVersionRepository
        .findById(versionId)
        .orElseThrow(
            () -> {
              log.info("버전 찾기 실패 - versionId: {}", versionId);
              return new CustomException(VersionErrorCode.VERSION_NOT_FOUND);
            });
  }
}
