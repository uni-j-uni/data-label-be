/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.signtelecom.datalabelbe.domain.assignment.entity.Assignment;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentRepository;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.image.exception.ImageErrorCode;
import com.signtelecom.datalabelbe.domain.image.repository.ImageRepository;
import com.signtelecom.datalabelbe.domain.label.dto.request.KeypointRequest;
import com.signtelecom.datalabelbe.domain.label.dto.request.PoseLabelSaveRequest;
import com.signtelecom.datalabelbe.domain.label.dto.request.PoseObjectRequest;
import com.signtelecom.datalabelbe.domain.label.dto.response.PoseLabelResponse;
import com.signtelecom.datalabelbe.domain.label.entity.KeypointType;
import com.signtelecom.datalabelbe.domain.label.entity.PoseLabel;
import com.signtelecom.datalabelbe.domain.label.entity.PoseObject;
import com.signtelecom.datalabelbe.domain.label.exception.LabelErrorCode;
import com.signtelecom.datalabelbe.domain.label.mapper.LabelMapper;
import com.signtelecom.datalabelbe.domain.label.repository.PoseLabelRepository;
import com.signtelecom.datalabelbe.domain.user.entity.Role;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.domain.user.service.UserService;
import com.signtelecom.datalabelbe.global.exception.CustomException;
import com.signtelecom.datalabelbe.global.exception.ErrorGuard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

  /** 이 상태에서만 라벨을 수정할 수 있음. 반려 건도 이어서 수정 가능 */
  private static final Set<AssignmentStatus> EDITABLE_STATUSES =
      EnumSet.of(AssignmentStatus.IN_PROGRESS, AssignmentStatus.REJECTED);

  private final PoseLabelRepository poseLabelRepository;
  private final ImageRepository imageRepository;
  private final AssignmentRepository assignmentRepository;
  private final LabelMapper labelMapper;
  private final UserService userService;

  @Override
  @Transactional(readOnly = true)
  public PoseLabelResponse getLabel(Long imageId) {

    Image image = findImage(imageId);
    Assignment assignment = assignmentRepository.findByImageId(imageId).orElse(null);
    User currentUser = userService.getCurrentUser();

    if (!canView(image, assignment, currentUser)) {
      log.warn("라벨 조회 권한 없음 - imageId: {}, userId: {}", imageId, currentUser.getId());
      ErrorGuard.fail(LabelErrorCode.NOT_ASSIGNED_WORKER);
    }

    PoseLabel label = poseLabelRepository.findByImageId(imageId).orElse(null);
    AssignmentStatus status = statusOf(assignment);

    log.info(
        "라벨 조회 - imageId: {}, 객체 수: {}", imageId, label == null ? 0 : label.getObjects().size());

    return labelMapper.toPoseLabelResponse(
        label,
        image,
        assignment == null ? null : assignment.getId(),
        status,
        isEditableBy(assignment, currentUser));
  }

  @Override
  @Transactional
  public PoseLabelResponse saveLabel(Long imageId, PoseLabelSaveRequest request) {

    Image image = findImage(imageId);
    Assignment assignment = assignmentRepository.findByImageId(imageId).orElse(null);
    User currentUser = userService.getCurrentUser();

    // 저장은 담당 작업자만 할 수 있음. 검수자는 읽기 전용으로만 봄
    if (assignment == null || !assignment.isAssignedTo(currentUser)) {
      log.warn("담당 작업자가 아닌 저장 시도 - imageId: {}, userId: {}", imageId, currentUser.getId());
      ErrorGuard.fail(LabelErrorCode.NOT_ASSIGNED_WORKER);
    }

    if (!EDITABLE_STATUSES.contains(assignment.getStatus())) {
      log.info("수정 불가 상태에서 저장 시도 - imageId: {}, status: {}", imageId, assignment.getStatus());
      ErrorGuard.fail(LabelErrorCode.NOT_EDITABLE);
    }

    List<PoseObjectRequest> objectRequests = request.getObjects();
    objectRequests.forEach(o -> validateObject(o, image));

    // 기존 객체·키포인트는 벌크로 먼저 지움. 라벨의 objects 컬렉션이 아직 로딩되지 않은 상태여야
    // 아래 replaceObjects 의 clear() 가 빈 목록을 읽어 행 단위 orphan delete 를 하지 않음
    poseLabelRepository.deleteKeypointsByImageId(imageId);
    poseLabelRepository.deleteObjectsByImageId(imageId);

    PoseLabel label =
        poseLabelRepository
            .findByImageId(imageId)
            .orElseGet(() -> poseLabelRepository.save(PoseLabel.builder().image(image).build()));

    List<PoseObject> objects = new ArrayList<>();
    for (int i = 0; i < objectRequests.size(); i++) {
      objects.add(labelMapper.toPoseObject(objectRequests.get(i), i));
    }
    label.replaceObjects(objects);

    // 응답에 objectId 를 담으려면 여기서 flush 해야 함. 커밋까지 미루면 식별자가 아직 null 이라,
    // 검수의 객체 단위 코멘트(targetObjectId)가 참조할 값을 클라이언트가 받지 못함
    poseLabelRepository.flush();

    log.info("라벨 임시 저장 - imageId: {}, 객체 수: {}", imageId, objects.size());

    return labelMapper.toPoseLabelResponse(
        label, image, assignment.getId(), assignment.getStatus(), true);
  }

  /**
   * 객체 하나를 검증
   *
   * <p>API를 직접 호출하면 클라이언트 검증을 건너뛸 수 있으므로 서버에서 다시 검증
   */
  private void validateObject(PoseObjectRequest request, Image image) {

    List<KeypointRequest> keypoints = request.getKeypoints();

    if (keypoints.size() != KeypointType.COUNT) {
      log.info("키포인트 개수 불일치 - 기대: {}, 실제: {}", KeypointType.COUNT, keypoints.size());
      ErrorGuard.fail(LabelErrorCode.INVALID_KEYPOINT_COUNT);
    }

    Set<KeypointType> types = EnumSet.noneOf(KeypointType.class);
    for (KeypointRequest keypoint : keypoints) {
      if (!types.add(keypoint.getType())) {
        log.info("키포인트 종류 중복 - type: {}", keypoint.getType());
        ErrorGuard.fail(LabelErrorCode.DUPLICATE_KEYPOINT_TYPE);
      }
      validateInBounds(keypoint.getX(), keypoint.getY(), image);
    }

    // 17종이 모두 들어왔는지 (중복이 없고 개수가 17이면 자동으로 보장되지만, 의도를 드러내기 위해 남김)
    ErrorGuard.throwIf(types.size() != KeypointType.COUNT, LabelErrorCode.INVALID_KEYPOINT_COUNT);

    if (request.getBbox().getWidth() <= 0 || request.getBbox().getHeight() <= 0) {
      log.info(
          "바운딩 박스 크기 오류 - width: {}, height: {}",
          request.getBbox().getWidth(),
          request.getBbox().getHeight());
      ErrorGuard.fail(LabelErrorCode.INVALID_BBOX);
    }

    validateInBounds(request.getBbox().getX(), request.getBbox().getY(), image);
    validateInBounds(
        request.getBbox().getX() + request.getBbox().getWidth(),
        request.getBbox().getY() + request.getBbox().getHeight(),
        image);
  }

  /** 좌표가 이미지 범위 안에 있는지 확인 */
  private void validateInBounds(Double x, Double y, Image image) {
    if (x < 0 || y < 0 || x > image.getWidth() || y > image.getHeight()) {
      log.info("좌표 범위 초과 - x: {}, y: {}, 이미지: {}x{}", x, y, image.getWidth(), image.getHeight());
      ErrorGuard.fail(LabelErrorCode.COORDINATE_OUT_OF_BOUNDS);
    }
  }

  /** 배치 소유자·관리자·담당 작업자면 조회할 수 있음 */
  private boolean canView(Image image, Assignment assignment, User user) {
    return image.getBatch().isOwnedBy(user)
        || user.getRole() == Role.ROLE_ADMIN
        || (assignment != null && assignment.isAssignedTo(user));
  }

  /** 담당 작업자이고 수정 가능한 상태일 때만 true */
  private boolean isEditableBy(Assignment assignment, User user) {
    return assignment != null
        && assignment.isAssignedTo(user)
        && EDITABLE_STATUSES.contains(assignment.getStatus());
  }

  /** 할당이 없으면 미할당을 뜻하는 PENDING 으로 봄 */
  private AssignmentStatus statusOf(Assignment assignment) {
    return assignment == null ? AssignmentStatus.PENDING : assignment.getStatus();
  }

  private Image findImage(Long imageId) {
    return imageRepository
        .findById(imageId)
        .orElseThrow(
            () -> {
              log.info("이미지 찾기 실패 - imageId: {}", imageId);
              return new CustomException(ImageErrorCode.IMAGE_NOT_FOUND);
            });
  }
}
