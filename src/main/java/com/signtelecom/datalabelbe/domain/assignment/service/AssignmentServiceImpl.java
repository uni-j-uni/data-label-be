/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.signtelecom.datalabelbe.domain.assignment.dto.request.AssignmentCreateRequest;
import com.signtelecom.datalabelbe.domain.assignment.dto.request.AssignmentReleaseRequest;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentCreateResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentReleaseResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.MyAssignmentSummaryResponse;
import com.signtelecom.datalabelbe.domain.assignment.entity.Assignment;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentAction;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentHistory;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.domain.assignment.exception.AssignmentErrorCode;
import com.signtelecom.datalabelbe.domain.assignment.mapper.AssignmentMapper;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentHistoryRepository;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentRepository;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.image.exception.ImageErrorCode;
import com.signtelecom.datalabelbe.domain.image.repository.ImageRepository;
import com.signtelecom.datalabelbe.domain.image.service.ImageBatchService;
import com.signtelecom.datalabelbe.domain.label.entity.PoseLabel;
import com.signtelecom.datalabelbe.domain.label.entity.PoseObject;
import com.signtelecom.datalabelbe.domain.label.exception.LabelErrorCode;
import com.signtelecom.datalabelbe.domain.label.repository.PoseLabelRepository;
import com.signtelecom.datalabelbe.domain.label.repository.PoseLabelRepository.ImageObjectCount;
import com.signtelecom.datalabelbe.domain.review.service.ReviewService;
import com.signtelecom.datalabelbe.domain.user.entity.Role;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.domain.user.repository.UserRepository;
import com.signtelecom.datalabelbe.domain.user.service.UserService;
import com.signtelecom.datalabelbe.global.exception.CustomException;
import com.signtelecom.datalabelbe.global.exception.ErrorGuard;
import com.signtelecom.datalabelbe.global.page.PageGuard;
import com.signtelecom.datalabelbe.global.page.mapper.InfiniteMapper;
import com.signtelecom.datalabelbe.global.page.response.InfiniteResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

  private final AssignmentRepository assignmentRepository;
  private final AssignmentHistoryRepository assignmentHistoryRepository;
  private final ImageRepository imageRepository;
  private final UserRepository userRepository;
  private final ImageBatchService imageBatchService;
  private final AssignmentMapper assignmentMapper;
  private final UserService userService;
  private final PoseLabelRepository poseLabelRepository;
  private final ReviewService reviewService;
  private final InfiniteMapper infiniteMapper;

  @Override
  @Transactional
  public AssignmentCreateResponse createAssignments(AssignmentCreateRequest request) {

    List<Long> imageIds = request.getImageIds();

    ErrorGuard.throwIf(
        imageIds == null || imageIds.isEmpty(), AssignmentErrorCode.NO_IMAGE_SELECTED);

    User worker = findWorker(request.getWorkerId());

    User admin = userService.getCurrentUser();
    Map<Long, Assignment> existing = findExistingByImageId(imageIds);

    List<Assignment> assigned = new ArrayList<>();
    List<Assignment> created = new ArrayList<>();
    List<Long> skippedImageIds = new ArrayList<>();
    List<AssignmentHistory> histories = new ArrayList<>();

    // 요청 순서 유지
    for (Long imageId : imageIds) {
      Assignment current = existing.get(imageId);

      if (current == null) {
        Image image = findImage(imageId);
        Assignment newAssignment = assignmentMapper.toAssignment(image, worker, admin);
        created.add(newAssignment);
        assigned.add(newAssignment);
        histories.add(
            assignmentMapper.toHistory(image, AssignmentAction.ASSIGN, worker, null, admin));
        continue;
      }

      // 승인된 건은 담당자를 바꾸면 검수 이력과 맞지 않으므로 건너뜀
      if (current.isApproved()) {
        log.info("승인 완료된 할당은 재할당하지 않음 - imageId: {}", imageId);
        skippedImageIds.add(imageId);
        continue;
      }

      // 라벨 데이터는 이미지에 속하므로 담당자만 바꾸고 진행 상태는 유지
      // 같은 작업자에게 다시 배정하면 변경이 없으므로 이력을 남기지 않음
      if (!current.isAssignedTo(worker)) {
        histories.add(
            assignmentMapper.toHistory(
                current.getImage(), AssignmentAction.REASSIGN, worker, current.getWorker(), admin));
        current.reassignTo(worker);
      }
      assigned.add(current);
    }

    assignmentRepository.saveAll(created);
    assignmentHistoryRepository.saveAll(histories);

    log.info(
        "작업 할당 - workerId: {}, 할당: {}건(신규 {}건, 담당자 교체 {}건), 건너뜀: {}건",
        worker.getId(),
        assigned.size(),
        created.size(),
        assigned.size() - created.size(),
        skippedImageIds.size());

    List<AssignmentResponse> assignments = toResponses(assigned);

    return AssignmentCreateResponse.builder()
        .assignedCount(assignments.size())
        .skippedCount(skippedImageIds.size())
        .assignments(assignments)
        .skippedImageIds(skippedImageIds)
        .build();
  }

  @Override
  @Transactional
  public AssignmentReleaseResponse releaseAssignments(AssignmentReleaseRequest request) {

    List<Long> imageIds = request.getImageIds();

    ErrorGuard.throwIf(
        imageIds == null || imageIds.isEmpty(), AssignmentErrorCode.NO_IMAGE_SELECTED);

    Map<Long, Assignment> existing = findExistingByImageId(imageIds);
    Set<Long> labeledImageIds = new HashSet<>(poseLabelRepository.findLabeledImageIdsIn(imageIds));

    List<Assignment> releasable = new ArrayList<>();
    List<Long> releasedImageIds = new ArrayList<>();
    List<Long> skippedImageIds = new ArrayList<>();

    // 요청 순서 유지. 미할당 이미지는 released·skipped 어디에도 넣지 않음
    for (Long imageId : new LinkedHashSet<>(imageIds)) {
      Assignment assignment = existing.get(imageId);

      if (assignment == null) {
        continue;
      }

      // 손대지 않은 할당(진행중 + 라벨 미저장)만 해제. 작업이 시작된 건은 담당자 변경(재할당)으로 처리
      if (assignment.getStatus() == AssignmentStatus.IN_PROGRESS
          && !labeledImageIds.contains(imageId)) {
        releasable.add(assignment);
        releasedImageIds.add(imageId);
      } else {
        skippedImageIds.add(imageId);
      }
    }

    User admin = userService.getCurrentUser();
    List<AssignmentHistory> histories =
        releasable.stream()
            .map(
                a ->
                    assignmentMapper.toHistory(
                        a.getImage(), AssignmentAction.RELEASE, a.getWorker(), null, admin))
            .toList();

    assignmentRepository.deleteAll(releasable);
    assignmentHistoryRepository.saveAll(histories);

    log.info(
        "작업 할당 해제 - 해제: {}건 {}, 건너뜀: {}건 {}",
        releasedImageIds.size(),
        releasedImageIds,
        skippedImageIds.size(),
        skippedImageIds);

    return AssignmentReleaseResponse.builder()
        .releasedCount(releasedImageIds.size())
        .releasedImageIds(releasedImageIds)
        .skippedCount(skippedImageIds.size())
        .skippedImageIds(skippedImageIds)
        .build();
  }

  @Override
  @Transactional
  public AssignmentResponse submit(Long assignmentId) {

    Assignment assignment =
        assignmentRepository
            .findById(assignmentId)
            .orElseThrow(
                () -> {
                  log.info("할당 찾기 실패 - assignmentId: {}", assignmentId);
                  return new CustomException(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND);
                });

    User currentUser = userService.getCurrentUser();

    if (!assignment.isAssignedTo(currentUser)) {
      log.warn("담당자가 아닌 제출 시도 - assignmentId: {}, userId: {}", assignmentId, currentUser.getId());
      ErrorGuard.fail(LabelErrorCode.NOT_ASSIGNED_WORKER);
    }

    // 반려 건도 재제출 가능
    if (assignment.getStatus() != AssignmentStatus.IN_PROGRESS
        && assignment.getStatus() != AssignmentStatus.REJECTED) {
      log.info("제출 불가 상태 - assignmentId: {}, status: {}", assignmentId, assignment.getStatus());
      ErrorGuard.fail(LabelErrorCode.ALREADY_SUBMITTED);
    }

    // 빈 라벨이 검수 대기열에 쌓이지 않도록 막음
    Image image = assignment.getImage();
    Long imageId = image.getId();
    PoseLabel label =
        poseLabelRepository
            .findByImageId(imageId)
            .orElseThrow(
                () -> {
                  log.info("라벨 없이 제출 시도 - assignmentId: {}", assignmentId);
                  return new CustomException(LabelErrorCode.NO_OBJECT_TO_SUBMIT);
                });

    if (label.getObjects().isEmpty()) {
      log.info("객체 없이 제출 시도 - assignmentId: {}, imageId: {}", assignmentId, imageId);
      ErrorGuard.fail(LabelErrorCode.NO_OBJECT_TO_SUBMIT);
    }

    assignment.changeStatus(AssignmentStatus.PENDING_REVIEW);

    // 반려 후 재제출하면 새 이력이 쌓여 과거 제출 건을 그대로 조회할 수 있음
    int labeledKeypointCount =
        (int) label.getObjects().stream().mapToLong(PoseObject::labeledKeypointCount).sum();
    reviewService.createSubmission(
        image, currentUser, label.getObjects().size(), labeledKeypointCount);

    log.info(
        "검수 요청 - assignmentId: {}, imageId: {}, workerId: {}",
        assignmentId,
        imageId,
        currentUser.getId());

    return assignmentMapper.toAssignmentResponse(
        assignment, poseLabelRepository.countObjectsByImageId(imageId), true);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AssignmentResponse> getAssignmentsByBatch(Long batchId) {

    // 배치 접근 권한 검증을 이미지 도메인과 동일한 규칙으로 재사용
    imageBatchService.getOwnedBatch(batchId);

    List<Assignment> assignments = assignmentRepository.findByBatchId(batchId);

    log.info("배치 할당 현황 조회 - batchId: {}, 할당 건수: {}", batchId, assignments.size());

    return toResponses(assignments);
  }

  @Override
  @Transactional(readOnly = true)
  public InfiniteResponse<AssignmentResponse> getMyAssignments(
      List<AssignmentStatus> statuses, Long cursor, int size) {

    User currentUser = userService.getCurrentUser();
    int limit = PageGuard.sizeOf(size);

    // status 를 생략하면 전체 상태. IN 절에 빈 목록을 넘길 수 없으므로 모든 상태로 채움
    List<AssignmentStatus> targets =
        statuses == null || statuses.isEmpty() ? List.of(AssignmentStatus.values()) : statuses;

    // 한 건 더 읽어 다음 묶음이 있는지 판단
    List<Assignment> fetched =
        assignmentRepository.findByWorkerIdBeforeCursor(
            currentUser.getId(), targets, cursor, PageRequest.of(0, limit + 1));
    boolean hasNext = fetched.size() > limit;
    List<Assignment> assignments = hasNext ? fetched.subList(0, limit) : fetched;
    Long lastCursor = assignments.isEmpty() ? null : assignments.getLast().getId();

    log.info(
        "내 작업 목록 조회 - workerId: {}, status: {}, cursor: {}, 건수: {}, hasNext: {}",
        currentUser.getId(),
        statuses,
        cursor,
        assignments.size(),
        hasNext);

    return infiniteMapper.toInfiniteResponse(
        toResponses(assignments), lastCursor, hasNext, assignments.size());
  }

  @Override
  @Transactional(readOnly = true)
  public MyAssignmentSummaryResponse getMyAssignmentSummary() {

    User currentUser = userService.getCurrentUser();

    // 건이 없는 상태도 0으로 내려 클라이언트가 키 존재 여부를 따지지 않게 함
    Map<AssignmentStatus, Long> statusCounts = new EnumMap<>(AssignmentStatus.class);
    for (AssignmentStatus status : AssignmentStatus.values()) {
      statusCounts.put(status, 0L);
    }
    assignmentRepository
        .countGroupedByStatusByWorkerId(currentUser.getId())
        .forEach(c -> statusCounts.put(c.getStatus(), c.getCount()));

    long totalCount = statusCounts.values().stream().mapToLong(Long::longValue).sum();
    Long nextImageId =
        assignmentRepository
            .findFirstByWorkerIdAndStatusInOrderByIdDesc(
                currentUser.getId(),
                List.of(AssignmentStatus.IN_PROGRESS, AssignmentStatus.REJECTED))
            .map(a -> a.getImage().getId())
            .orElse(null);

    log.info(
        "내 작업 현황 조회 - workerId: {}, 전체: {}건, 이어서 작업: {}",
        currentUser.getId(),
        totalCount,
        nextImageId);

    return MyAssignmentSummaryResponse.builder()
        .totalCount(totalCount)
        .statusCounts(statusCounts)
        .nextImageId(nextImageId)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> getMyImageIds(Long batchId) {

    User currentUser = userService.getCurrentUser();
    List<Long> imageIds =
        assignmentRepository.findImageIdsByWorkerIdAndBatchId(currentUser.getId(), batchId);

    log.info(
        "내 작업 이미지 식별자 조회 - workerId: {}, batchId: {}, 건수: {}",
        currentUser.getId(),
        batchId,
        imageIds.size());

    return imageIds;
  }

  /**
   * 할당 목록을 응답 DTO로 변환
   *
   * <p>객체 수와 라벨 저장 여부는 할당마다 조회하지 않고 이미지 식별자들을 모아 한 번에 조회
   */
  private List<AssignmentResponse> toResponses(List<Assignment> assignments) {

    if (assignments.isEmpty()) {
      return List.of();
    }

    List<Long> imageIds = assignments.stream().map(a -> a.getImage().getId()).toList();
    Map<Long, Long> objectCounts =
        poseLabelRepository.countObjectsGroupedByImageIdIn(imageIds).stream()
            .collect(
                Collectors.toMap(ImageObjectCount::getImageId, ImageObjectCount::getObjectCount));
    Set<Long> labeledImageIds = new HashSet<>(poseLabelRepository.findLabeledImageIdsIn(imageIds));

    return assignments.stream()
        .map(
            a ->
                assignmentMapper.toAssignmentResponse(
                    a,
                    objectCounts.getOrDefault(a.getImage().getId(), 0L),
                    labeledImageIds.contains(a.getImage().getId())))
        .toList();
  }

  /** 이미지 식별자로 바로 찾을 수 있도록 기존 할당을 맵으로 모음 */
  private Map<Long, Assignment> findExistingByImageId(List<Long> imageIds) {
    return assignmentRepository.findByImageIdIn(imageIds).stream()
        .collect(Collectors.toMap(a -> a.getImage().getId(), Function.identity()));
  }

  /** 배정 대상 작업자를 조회하고 ROLE_USER인지 확인 */
  private User findWorker(Long workerId) {

    User worker =
        userRepository
            .findByIdAndDeletedAtIsNull(workerId)
            .orElseThrow(
                () -> {
                  log.info("작업자 찾기 실패(없거나 탈퇴) - workerId: {}", workerId);
                  return new CustomException(AssignmentErrorCode.WORKER_NOT_FOUND);
                });

    // 관리자·개발자 계정에 라벨링 작업을 배정하는 것은 의도된 사용이 아님
    if (worker.getRole() != Role.ROLE_USER) {
      log.info("작업자가 아닌 계정에 할당 시도 - workerId: {}, role: {}", workerId, worker.getRole());
      ErrorGuard.fail(AssignmentErrorCode.NOT_A_WORKER);
    }

    return worker;
  }

  /** 할당할 이미지를 조회 */
  private Image findImage(Long imageId) {
    return imageRepository
        .findById(imageId)
        .orElseThrow(
            () -> {
              log.info("할당할 이미지 찾기 실패 - imageId: {}", imageId);
              return new CustomException(ImageErrorCode.IMAGE_NOT_FOUND);
            });
  }
}
