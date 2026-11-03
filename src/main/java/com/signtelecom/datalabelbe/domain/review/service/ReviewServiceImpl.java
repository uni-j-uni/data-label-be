/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signtelecom.datalabelbe.domain.assignment.entity.Assignment;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentRepository;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.image.exception.ImageErrorCode;
import com.signtelecom.datalabelbe.domain.image.repository.ImageRepository;
import com.signtelecom.datalabelbe.domain.label.dto.response.PoseLabelResponse;
import com.signtelecom.datalabelbe.domain.label.entity.PoseLabel;
import com.signtelecom.datalabelbe.domain.label.entity.PoseObject;
import com.signtelecom.datalabelbe.domain.label.exception.LabelErrorCode;
import com.signtelecom.datalabelbe.domain.label.mapper.LabelMapper;
import com.signtelecom.datalabelbe.domain.label.repository.PoseLabelRepository;
import com.signtelecom.datalabelbe.domain.review.dto.request.CommentRequest;
import com.signtelecom.datalabelbe.domain.review.dto.request.ReviewRequest;
import com.signtelecom.datalabelbe.domain.review.dto.response.CommentResponse;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionDetailResponse;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionResponse;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionSummaryResponse;
import com.signtelecom.datalabelbe.domain.review.entity.Comment;
import com.signtelecom.datalabelbe.domain.review.entity.ReviewDecision;
import com.signtelecom.datalabelbe.domain.review.entity.Submission;
import com.signtelecom.datalabelbe.domain.review.entity.SubmissionSnapshot;
import com.signtelecom.datalabelbe.domain.review.entity.SubmissionStatus;
import com.signtelecom.datalabelbe.domain.review.exception.ReviewErrorCode;
import com.signtelecom.datalabelbe.domain.review.mapper.ReviewMapper;
import com.signtelecom.datalabelbe.domain.review.repository.CommentRepository;
import com.signtelecom.datalabelbe.domain.review.repository.CommentRepository.SubmissionCommentCount;
import com.signtelecom.datalabelbe.domain.review.repository.SubmissionRepository;
import com.signtelecom.datalabelbe.domain.review.repository.SubmissionSnapshotRepository;
import com.signtelecom.datalabelbe.domain.user.entity.Role;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.domain.user.service.UserService;
import com.signtelecom.datalabelbe.global.exception.CustomException;
import com.signtelecom.datalabelbe.global.page.PageGuard;
import com.signtelecom.datalabelbe.global.page.mapper.PageMapper;
import com.signtelecom.datalabelbe.global.page.response.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private static final String SOURCE_LIVE = "LIVE";
  private static final String SOURCE_SNAPSHOT = "SNAPSHOT";

  private final SubmissionRepository submissionRepository;
  private final SubmissionSnapshotRepository submissionSnapshotRepository;
  private final CommentRepository commentRepository;
  private final PageMapper pageMapper;
  private final ImageRepository imageRepository;
  private final PoseLabelRepository poseLabelRepository;
  private final AssignmentRepository assignmentRepository;
  private final ReviewMapper reviewMapper;
  private final LabelMapper labelMapper;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void createSubmission(
      Image image, User submittedBy, int objectCount, int labeledKeypointCount) {

    Submission submission =
        submissionRepository.save(
            Submission.builder()
                .image(image)
                .submittedBy(submittedBy)
                .objectCount(objectCount)
                .labeledKeypointCount(labeledKeypointCount)
                .build());

    log.info(
        "제출 이력 생성 - submissionId: {}, imageId: {}, 객체: {}건",
        submission.getId(),
        image.getId(),
        objectCount);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<SubmissionResponse> getSubmissions(
      List<SubmissionStatus> statuses, Long batchId, int page, int size) {

    // status 를 생략하면 전체 상태. IN 절에 빈 목록을 넘길 수 없으므로 모든 상태로 채움
    List<SubmissionStatus> targets =
        statuses == null || statuses.isEmpty() ? List.of(SubmissionStatus.values()) : statuses;
    Page<Submission> submissions =
        submissionRepository.search(
            targets,
            batchId,
            PageGuard.pageOf(
                page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));

    // 제출마다 코멘트 수를 세지 않도록 이 페이지에 실린 제출만 한 번에 집계
    List<Long> submissionIds = submissions.map(Submission::getId).getContent();
    Map<Long, Long> commentCounts =
        submissionIds.isEmpty()
            ? Map.of()
            : commentRepository.countGroupedBySubmissionIdIn(submissionIds).stream()
                .collect(
                    Collectors.toMap(
                        SubmissionCommentCount::getSubmissionId,
                        SubmissionCommentCount::getCommentCount));

    log.info(
        "검수 목록 조회 - status: {}, batchId: {}, page: {}, 건수: {}/{}",
        statuses,
        batchId,
        page,
        submissions.getNumberOfElements(),
        submissions.getTotalElements());

    return pageMapper.toPageResponse(
        submissions.map(
            s -> reviewMapper.toSubmissionResponse(s, commentCounts.getOrDefault(s.getId(), 0L))));
  }

  @Override
  @Transactional(readOnly = true)
  public SubmissionSummaryResponse getSubmissionSummary(Long batchId) {

    // 건이 없는 상태도 0으로 내려 클라이언트가 키 존재 여부를 따지지 않게 함
    Map<SubmissionStatus, Long> statusCounts = new EnumMap<>(SubmissionStatus.class);
    for (SubmissionStatus status : SubmissionStatus.values()) {
      statusCounts.put(status, 0L);
    }
    submissionRepository
        .countGroupedByStatus(batchId)
        .forEach(c -> statusCounts.put(c.getStatus(), c.getCount()));

    long totalCount = statusCounts.values().stream().mapToLong(Long::longValue).sum();

    log.info("검수 현황 조회 - batchId: {}, 전체: {}건", batchId, totalCount);

    return SubmissionSummaryResponse.builder()
        .totalCount(totalCount)
        .statusCounts(statusCounts)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public SubmissionDetailResponse getSubmission(Long submissionId) {

    Submission submission = findSubmission(submissionId);
    User currentUser = userService.getCurrentUser();

    // 제출자 본인은 반려 사유를 읽고 재작업해야 하므로 조회를 허용
    if (currentUser.getRole() != Role.ROLE_ADMIN && !submission.isSubmittedBy(currentUser)) {
      log.warn("제출 건 접근 권한 없음 - submissionId: {}, userId: {}", submissionId, currentUser.getId());
      throw new CustomException(ReviewErrorCode.SUBMISSION_FORBIDDEN);
    }

    SubmissionSnapshot snapshot =
        submissionSnapshotRepository.findBySubmissionId(submissionId).orElse(null);
    Image image = submission.getImage();

    PoseLabelResponse label;
    String labelSource;

    if (snapshot != null) {
      // 승인된 건은 스냅샷을 반환. 이후 원본 라벨이 수정돼도 승인 당시 내용 유지
      label = reviewMapper.toPoseLabelResponse(snapshot, readPayload(snapshot), image.getId());
      labelSource = SOURCE_SNAPSHOT;
    } else {
      PoseLabel poseLabel = poseLabelRepository.findByImageId(image.getId()).orElse(null);
      label =
          labelMapper.toPoseLabelResponse(
              poseLabel, image, null, AssignmentStatus.PENDING_REVIEW, false);
      labelSource = SOURCE_LIVE;
    }

    List<CommentResponse> comments =
        submission.getComments().stream().map(reviewMapper::toCommentResponse).toList();

    log.info("검수 상세 조회 - submissionId: {}, 출처: {}", submissionId, labelSource);

    return SubmissionDetailResponse.builder()
        .submission(reviewMapper.toSubmissionResponse(submission, comments.size()))
        .label(label)
        .comments(comments)
        .labelSource(labelSource)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<CommentResponse> getImageComments(Long imageId) {

    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(
                () -> {
                  log.info("이미지 찾기 실패 - imageId: {}", imageId);
                  return new CustomException(ImageErrorCode.IMAGE_NOT_FOUND);
                });

    User currentUser = userService.getCurrentUser();
    boolean allowed =
        image.getBatch().isOwnedBy(currentUser)
            || currentUser.getRole() == Role.ROLE_ADMIN
            || assignmentRepository.existsByImageIdAndWorkerId(imageId, currentUser.getId());

    if (!allowed) {
      log.warn("코멘트 조회 권한 없음 - imageId: {}, userId: {}", imageId, currentUser.getId());
      throw new CustomException(ReviewErrorCode.SUBMISSION_FORBIDDEN);
    }

    List<Comment> comments = commentRepository.findByImageIdOrderByCreatedAtDesc(imageId);

    log.info("이미지 코멘트 조회 - imageId: {}, 코멘트: {}건", imageId, comments.size());

    return comments.stream().map(reviewMapper::toCommentResponse).toList();
  }

  @Override
  @Transactional
  public SubmissionResponse review(ReviewRequest request) {

    Submission submission = findSubmission(request.getSubmissionId());

    if (!submission.isPendingReview()) {
      log.info(
          "이미 검수된 건 - submissionId: {}, status: {}", submission.getId(), submission.getStatus());
      throw new CustomException(ReviewErrorCode.ALREADY_REVIEWED);
    }

    List<CommentRequest> commentRequests =
        request.getComments() == null ? List.of() : request.getComments();
    boolean reject = request.getDecision() == ReviewDecision.REJECT;

    // 재작업의 근거가 없으면 작업자가 무엇을 고쳐야 할지 알 수 없음
    if (reject && commentRequests.isEmpty()) {
      throw new CustomException(ReviewErrorCode.REJECT_REQUIRES_COMMENT);
    }

    User reviewer = userService.getCurrentUser();
    Image image = submission.getImage();
    Assignment assignment =
        assignmentRepository
            .findByImageId(image.getId())
            .orElseThrow(
                () -> {
                  log.warn("제출 건에 연결된 할당이 없음 - imageId: {}", image.getId());
                  return new CustomException(ReviewErrorCode.ASSIGNMENT_NOT_FOUND_FOR_SUBMISSION);
                });

    addComments(submission, commentRequests, reviewer, image.getId());

    if (reject) {
      submission.review(SubmissionStatus.REJECTED, reviewer, LocalDateTime.now());
      // 반려 상태를 할당에 반영
      // REJECTED 에서도 라벨 저장과 재제출이 허용되므로 하던 작업을 이어서 고칠 수 있음
      assignment.changeStatus(AssignmentStatus.REJECTED);

      log.info("반려 - submissionId: {}, 코멘트: {}건", submission.getId(), commentRequests.size());
    } else {
      submission.review(SubmissionStatus.APPROVED, reviewer, LocalDateTime.now());
      assignment.changeStatus(AssignmentStatus.APPROVED);
      submissionSnapshotRepository.save(createSnapshot(submission, image));

      log.info("승인 - submissionId: {}, 스냅샷 고정", submission.getId());
    }

    submissionRepository.flush();

    return reviewMapper.toSubmissionResponse(submission, submission.getComments().size());
  }

  /** 승인 시점의 라벨을 복사해 스냅샷을 만듦 */
  private SubmissionSnapshot createSnapshot(Submission submission, Image image) {

    PoseLabel label =
        poseLabelRepository
            .findByImageId(image.getId())
            .orElseThrow(
                () -> {
                  log.warn("승인 대상 라벨이 없음 - imageId: {}", image.getId());
                  return new CustomException(LabelErrorCode.LABEL_NOT_FOUND);
                });

    SnapshotPayload payload = reviewMapper.toSnapshotPayload(label);
    long labeledKeypointCount =
        label.getObjects().stream().mapToLong(PoseObject::labeledKeypointCount).sum();

    return SubmissionSnapshot.builder()
        .submission(submission)
        .imageWidth(image.getWidth())
        .imageHeight(image.getHeight())
        .objectCount(label.getObjects().size())
        .labeledKeypointCount((int) labeledKeypointCount)
        .payload(writePayload(payload))
        .build();
  }

  /**
   * 코멘트를 제출 건에 추가
   *
   * <p>요청은 대상 객체를 식별자로 주지만, 저장은 표시 순서로 함. 임시 저장 시 객체가 새로 만들어져 식별자가 바뀌기 때문
   */
  private void addComments(
      Submission submission, List<CommentRequest> requests, User author, Long imageId) {

    if (requests.isEmpty()) {
      return;
    }

    Map<Long, Integer> indexById = objectIndexById(imageId);

    for (CommentRequest request : requests) {
      Integer targetIndex = null;

      if (request.getTargetObjectId() != null) {
        targetIndex = indexById.get(request.getTargetObjectId());
        if (targetIndex == null) {
          log.info("코멘트 대상 객체 없음 - targetObjectId: {}", request.getTargetObjectId());
          throw new CustomException(ReviewErrorCode.INVALID_TARGET_OBJECT);
        }
      }

      submission.addComment(
          Comment.builder()
              .author(author)
              .targetObjectIndex(targetIndex)
              .text(request.getText())
              .build());
    }
  }

  /** 현재 라벨의 객체 식별자 → 표시 순서 매핑 */
  private Map<Long, Integer> objectIndexById(Long imageId) {
    return poseLabelRepository
        .findByImageId(imageId)
        .map(
            label ->
                label.getObjects().stream()
                    .collect(Collectors.toMap(PoseObject::getId, PoseObject::getIndex)))
        .orElseGet(Map::of);
  }

  private String writePayload(SnapshotPayload payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      log.error("스냅샷 직렬화 실패", e);
      throw new CustomException(ReviewErrorCode.SNAPSHOT_SERIALIZE_FAILED, e);
    }
  }

  private SnapshotPayload readPayload(SubmissionSnapshot snapshot) {
    try {
      return objectMapper.readValue(snapshot.getPayload(), SnapshotPayload.class);
    } catch (JsonProcessingException e) {
      log.error("스냅샷 역직렬화 실패 - snapshotId: {}", snapshot.getId(), e);
      throw new CustomException(ReviewErrorCode.SNAPSHOT_SERIALIZE_FAILED, e);
    }
  }

  private Submission findSubmission(Long submissionId) {
    return submissionRepository
        .findById(submissionId)
        .orElseThrow(
            () -> {
              log.info("제출 건 찾기 실패 - submissionId: {}", submissionId);
              return new CustomException(ReviewErrorCode.SUBMISSION_NOT_FOUND);
            });
  }
}
