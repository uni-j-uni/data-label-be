/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.signtelecom.datalabelbe.domain.assignment.entity.Assignment;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentHistoryRepository;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentRepository;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageResponse;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadFailure;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadItem;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadResponse;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.image.entity.ImageBatch;
import com.signtelecom.datalabelbe.domain.image.exception.ImageErrorCode;
import com.signtelecom.datalabelbe.domain.image.mapper.ImageMapper;
import com.signtelecom.datalabelbe.domain.image.repository.ImageRepository;
import com.signtelecom.datalabelbe.domain.label.repository.PoseLabelRepository;
import com.signtelecom.datalabelbe.domain.label.repository.PoseLabelRepository.ImageObjectCount;
import com.signtelecom.datalabelbe.domain.review.repository.SubmissionRepository;
import com.signtelecom.datalabelbe.domain.review.repository.SubmissionSnapshotRepository;
import com.signtelecom.datalabelbe.domain.user.entity.Role;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.domain.user.service.UserService;
import com.signtelecom.datalabelbe.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final ImageRepository imageRepository;
  private final ImageStorageService imageStorageService;
  private final ImageBatchService imageBatchService;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentHistoryRepository assignmentHistoryRepository;
  private final PoseLabelRepository poseLabelRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionSnapshotRepository submissionSnapshotRepository;
  private final UserService userService;
  private final ImageMapper imageMapper;

  @Override
  @Transactional
  public ImageUploadResponse createBatchAndUpload(String name, List<MultipartFile> files) {

    requireFiles(files);

    ImageBatch batch = imageBatchService.createBatch(name);

    return storeIntoBatch(batch, files);
  }

  @Override
  @Transactional
  public ImageUploadResponse uploadImages(Long batchId, List<MultipartFile> files) {

    requireFiles(files);

    ImageBatch batch = imageBatchService.getOwnedBatch(batchId);

    return storeIntoBatch(batch, files);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ImageResponse> getImages(Long batchId) {

    imageBatchService.getOwnedBatch(batchId);
    List<Image> images = imageRepository.findByBatchId(batchId);

    // 이미지마다 할당을 조회하지 않도록 배치 단위로 한 번에 가져와 병합
    Map<Long, Assignment> assignments =
        assignmentRepository.findByBatchId(batchId).stream()
            .collect(Collectors.toMap(a -> a.getImage().getId(), Function.identity()));

    // 객체 수도 이미지마다 세지 않고 배치 단위로 한 번에 집계
    Map<Long, Long> objectCounts =
        poseLabelRepository.countObjectsGroupedByImageId(batchId).stream()
            .collect(
                Collectors.toMap(ImageObjectCount::getImageId, ImageObjectCount::getObjectCount));

    Set<Long> labeledImageIds =
        new HashSet<>(poseLabelRepository.findLabeledImageIdsByBatchId(batchId));

    log.info(
        "이미지 목록 조회 - batchId: {}, 이미지 수: {}, 할당 수: {}", batchId, images.size(), assignments.size());

    return images.stream()
        .map(
            image ->
                imageMapper.toImageResponse(
                    image,
                    assignments.get(image.getId()),
                    objectCounts.getOrDefault(image.getId(), 0L),
                    labeledImageIds.contains(image.getId())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ImageResponse getImage(Long batchId, Long imageId) {

    Image image = getViewableImage(batchId, imageId);
    Assignment assignment = assignmentRepository.findByImageId(imageId).orElse(null);

    log.info("이미지 상세 조회 - batchId: {}, imageId: {}", batchId, imageId);

    return imageMapper.toImageResponse(
        image,
        assignment,
        poseLabelRepository.countObjectsByImageId(imageId),
        poseLabelRepository.existsByImageId(imageId));
  }

  @Override
  @Transactional(readOnly = true)
  public ImageFile loadImageFile(Long batchId, Long imageId, Integer width) {

    Image image = getViewableImage(batchId, imageId);

    if (width == null) {
      Resource original = imageStorageService.load(image.getStoragePath());

      log.debug("이미지 원본 조회 - batchId: {}, imageId: {}", batchId, imageId);

      return new ImageFile(
          original, image.getContentType(), image.getSizeBytes(), image.getOriginalFilename());
    }

    Resource thumbnail = imageStorageService.loadThumbnail(image.getStoragePath(), width);

    log.debug("이미지 썸네일 조회 - batchId: {}, imageId: {}, width: {}", batchId, imageId, width);

    return new ImageFile(
        thumbnail,
        MediaType.IMAGE_JPEG_VALUE,
        resourceLength(thumbnail),
        thumbnailFilename(image.getOriginalFilename(), width));
  }

  /** 썸네일 파일 크기. 읽지 못하면 Content-Length 를 생략하도록 0 을 돌려줌 */
  private long resourceLength(Resource resource) {
    try {
      return resource.contentLength();
    } catch (IOException e) {
      log.warn("썸네일 크기 조회 실패", e);
      return 0;
    }
  }

  /** 다운로드 시 원본과 구분되도록 너비를 붙인 파일명을 만듦 */
  private String thumbnailFilename(String originalFilename, int width) {
    int dot = originalFilename.lastIndexOf('.');
    String base = dot < 0 ? originalFilename : originalFilename.substring(0, dot);
    return "%s_w%d.jpg".formatted(base, width);
  }

  @Override
  @Transactional
  public void deleteImage(Long batchId, Long imageId) {

    Image image = getImageInBatch(batchId, imageId);

    // 할당·할당 이력·라벨·제출이 이미지를 참조하고 외래키가 NO ACTION 이므로, 이미지보다 먼저 모두 지워야 제약 위반이 나지 않음
    // 스냅샷은 제출과 JPA 연관이 없으므로 제출보다 먼저 벌크로 지움
    // 제출과 라벨은 벌크 delete 를 쓰면 안 됨. 벌크 JPQL 은 JPA cascade 를 건너뛰어
    // Comment, PoseObject/Keypoint 가 남고 다시 제약 위반이 남
    submissionSnapshotRepository.deleteByImageId(imageId);
    submissionRepository.deleteAll(submissionRepository.findByImageId(imageId));
    poseLabelRepository.findByImageId(imageId).ifPresent(poseLabelRepository::delete);
    int deletedAssignments = assignmentRepository.deleteByImageId(imageId);
    assignmentHistoryRepository.deleteByImageId(imageId);
    imageRepository.delete(image);
    imageStorageService.delete(image.getStoragePath());
    imageStorageService.deleteThumbnails(image.getStoragePath());

    log.info(
        "이미지 삭제 - batchId: {}, imageId: {}, 삭제된 할당: {}건", batchId, imageId, deletedAssignments);
  }

  /**
   * 파일들을 하나씩 검증해 배치에 저장
   *
   * <p>파일 단위로 예외를 잡아내므로 일부 파일이 실패해도 통과한 파일은 그대로 저장됨. 응답의 items 는 성공 건이 먼저, 실패 건이 나중에 오며 각 그룹 안에서는
   * 요청 순서를 유지함
   */
  private ImageUploadResponse storeIntoBatch(ImageBatch batch, List<MultipartFile> files) {

    List<StoredImage> stored = new ArrayList<>();
    List<Image> images = new ArrayList<>();
    List<ImageUploadFailure> failures = new ArrayList<>();

    for (MultipartFile file : files) {
      String filename = resolveOriginalFilename(file);

      StoredImage storedImage = null;
      try {
        storedImage = imageStorageService.store(batch.getId(), file);
        images.add(imageMapper.toImage(batch, filename, storedImage));
        stored.add(storedImage);
      } catch (CustomException e) {
        // 검증 실패는 요청 전체를 중단시키지 않고 사유만 모아 응답에 담음
        log.info("파일 검증 실패 - filename: {}, reason: {}", filename, e.getMessage());
        failures.add(imageMapper.toFailure(filename, e.getMessage()));
      } catch (RuntimeException e) {
        // 깨진 이미지 헤더 등 예상 밖의 예외도 해당 파일의 실패로만 처리해 나머지 파일의 저장을 막지 않음
        log.warn("파일 처리 중 예상치 못한 오류 - filename: {}", filename, e);
        if (storedImage != null) {
          imageStorageService.delete(storedImage.storagePath());
        }
        failures.add(imageMapper.toFailure(filename, ImageErrorCode.INVALID_IMAGE.getMessage()));
      }
    }

    List<Image> savedImages;
    try {
      savedImages = imageRepository.saveAll(images);
    } catch (RuntimeException e) {
      // DB 트랜잭션은 롤백되지만 이미 기록된 파일은 남으므로 직접 정리
      stored.forEach(storedImage -> imageStorageService.delete(storedImage.storagePath()));
      throw e;
    }

    List<ImageUploadItem> items = new ArrayList<>();
    savedImages.forEach(image -> items.add(imageMapper.toSuccessItem(image)));
    failures.forEach(failure -> items.add(imageMapper.toFailedItem(failure.getFilename())));

    log.info(
        "이미지 업로드 완료 - batchId: {}, 성공: {}건, 실패: {}건",
        batch.getId(),
        savedImages.size(),
        failures.size());

    return imageMapper.toImageUploadResponse(batch, items, failures);
  }

  /**
   * 썸네일·원본 조회가 허용되는 이미지를 반환
   *
   * <p>배치 소유자와 ADMIN 외에, 그 이미지를 할당받은 작업자도 볼 수 있어야 함. 작업자는 남이 만든 배치의 이미지를 작업하므로 배치 소유권만으로는 판단할 수 없음
   */
  private Image getViewableImage(Long batchId, Long imageId) {

    Image image = findImageInBatch(batchId, imageId);
    User currentUser = userService.getCurrentUser();

    boolean allowed =
        image.getBatch().isOwnedBy(currentUser)
            || currentUser.getRole() == Role.ROLE_ADMIN
            || assignmentRepository.existsByImageIdAndWorkerId(imageId, currentUser.getId());

    if (!allowed) {
      log.warn("이미지 조회 권한 없음 - imageId: {}, userId: {}", imageId, currentUser.getId());
      throw new CustomException(ImageErrorCode.IMAGE_BATCH_FORBIDDEN);
    }

    return image;
  }

  /**
   * 배치 접근 권한을 검증한 뒤, 그 배치에 실제로 속한 이미지를 조회
   *
   * <p>다른 배치의 이미지 식별자를 넘겨 우회하는 것을 막기 위해 소속까지 확인
   */
  private Image getImageInBatch(Long batchId, Long imageId) {

    imageBatchService.getOwnedBatch(batchId);

    return findImageInBatch(batchId, imageId);
  }

  /**
   * 이미지를 조회하고 해당 배치에 실제로 속하는지 확인 (권한 검증 없음)
   *
   * <p>다른 배치의 이미지 식별자를 넘겨 우회하는 것을 막기 위해 소속까지 확인
   */
  private Image findImageInBatch(Long batchId, Long imageId) {

    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(
                () -> {
                  log.info("이미지 찾기 실패 - imageId: {}", imageId);
                  return new CustomException(ImageErrorCode.IMAGE_NOT_FOUND);
                });

    if (!image.getBatch().getId().equals(batchId)) {
      log.info("배치에 속하지 않은 이미지 - batchId: {}, imageId: {}", batchId, imageId);
      throw new CustomException(ImageErrorCode.IMAGE_NOT_FOUND);
    }

    return image;
  }

  /** 파일이 하나도 전달되지 않았는지 확인 */
  private void requireFiles(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      throw new CustomException(ImageErrorCode.NO_FILE_UPLOADED);
    }
  }

  /** 원본 파일명이 비어 있는 경우를 대비해 기본값을 채움 */
  private String resolveOriginalFilename(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    return (originalFilename == null || originalFilename.isBlank()) ? "unknown" : originalFilename;
  }
}
