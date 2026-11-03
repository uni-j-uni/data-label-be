/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.mapper;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.image.entity.ImageBatch;
import com.signtelecom.datalabelbe.domain.image.mapper.ImageMapper;
import com.signtelecom.datalabelbe.domain.label.dto.response.BoundingBoxResponse;
import com.signtelecom.datalabelbe.domain.label.dto.response.KeypointResponse;
import com.signtelecom.datalabelbe.domain.label.dto.response.PoseLabelResponse;
import com.signtelecom.datalabelbe.domain.label.dto.response.PoseObjectResponse;
import com.signtelecom.datalabelbe.domain.label.entity.KeypointType;
import com.signtelecom.datalabelbe.domain.label.entity.PoseLabel;
import com.signtelecom.datalabelbe.domain.label.entity.PoseObject;
import com.signtelecom.datalabelbe.domain.label.entity.Visibility;
import com.signtelecom.datalabelbe.domain.review.dto.response.CommentResponse;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionResponse;
import com.signtelecom.datalabelbe.domain.review.entity.Comment;
import com.signtelecom.datalabelbe.domain.review.entity.Submission;
import com.signtelecom.datalabelbe.domain.review.entity.SubmissionSnapshot;
import com.signtelecom.datalabelbe.domain.review.service.SnapshotPayload;
import com.signtelecom.datalabelbe.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

  private final ImageMapper imageMapper;

  /**
   * 제출 엔티티를 목록용 요약 DTO로 변환
   *
   * @param submission 제출 엔티티
   * @param commentCount 이 제출 건에 달린 코멘트 수
   * @return 제출 요약 DTO
   */
  public SubmissionResponse toSubmissionResponse(Submission submission, long commentCount) {
    Image image = submission.getImage();
    ImageBatch batch = image.getBatch();
    Long batchId = batch.getId();
    User reviewer = submission.getReviewedBy();

    return SubmissionResponse.builder()
        .submissionId(submission.getId())
        .imageId(image.getId())
        .batchId(batchId)
        .batchName(batch.getName())
        .commentCount(commentCount)
        .filename(image.getOriginalFilename())
        .url(imageMapper.toImageFileUrl(batchId, image.getId()))
        .thumbnailUrl(imageMapper.toThumbnailUrl(batchId, image.getId()))
        .submittedById(submission.getSubmittedBy().getId())
        .submittedByName(submission.getSubmittedBy().getName())
        .submittedAt(submission.getCreatedAt())
        .status(submission.getStatus())
        .objectCount(submission.getObjectCount())
        .labeledKeypointCount(submission.getLabeledKeypointCount())
        .reviewedByName(reviewer == null ? null : reviewer.getName())
        .reviewedAt(submission.getReviewedAt())
        .build();
  }

  /** 코멘트 엔티티를 응답 DTO로 변환 */
  public CommentResponse toCommentResponse(Comment comment) {
    return CommentResponse.builder()
        .commentId(comment.getId())
        .submissionId(comment.getSubmission().getId())
        .targetObjectIndex(comment.getTargetObjectIndex())
        .text(comment.getText())
        .authorId(comment.getAuthor().getId())
        .authorName(comment.getAuthor().getName())
        .createdAt(comment.getCreatedAt())
        .build();
  }

  /**
   * 라벨 내용을 복사해 스냅샷 페이로드를 만듦
   *
   * <p>키포인트는 COCO 순서로 정렬해 담음. export 시 배열 순서를 그대로 쓸 수 있게 하기 위함
   *
   * @param label 승인 시점의 라벨
   * @return 직렬화 대상 페이로드
   */
  public SnapshotPayload toSnapshotPayload(PoseLabel label) {
    List<SnapshotPayload.SnapshotObject> objects =
        label.getObjects().stream().map(this::toSnapshotObject).toList();

    return new SnapshotPayload(objects);
  }

  private SnapshotPayload.SnapshotObject toSnapshotObject(PoseObject object) {
    List<SnapshotPayload.SnapshotKeypoint> keypoints =
        object.getKeypoints().stream()
            .sorted(Comparator.comparingInt(k -> k.getType().getCocoIndex()))
            .map(
                k ->
                    new SnapshotPayload.SnapshotKeypoint(
                        k.getType().name(),
                        k.getType().getCocoIndex(),
                        k.getX(),
                        k.getY(),
                        k.getVisibility().getCode()))
            .toList();

    return new SnapshotPayload.SnapshotObject(
        object.getIndex(),
        object.getBboxX(),
        object.getBboxY(),
        object.getBboxWidth(),
        object.getBboxHeight(),
        keypoints);
  }

  /**
   * 승인 스냅샷을 라벨 응답 DTO로 되돌림
   *
   * <p>현재 라벨과 동일한 응답 형태로 맞춤. 수정 불가한 복사본이므로 editable 은 항상 false 이고 식별자는 없음
   *
   * @param snapshot 승인 스냅샷
   * @param payload 역직렬화된 페이로드
   * @param imageId 대상 이미지 식별자
   * @return 라벨 응답 DTO
   */
  public PoseLabelResponse toPoseLabelResponse(
      SubmissionSnapshot snapshot, SnapshotPayload payload, Long imageId) {

    List<PoseObjectResponse> objects =
        payload.objects().stream()
            .map(
                o ->
                    PoseObjectResponse.builder()
                        .objectId(null)
                        .index(o.index())
                        .bbox(
                            BoundingBoxResponse.builder()
                                .x(o.bboxX())
                                .y(o.bboxY())
                                .width(o.bboxWidth())
                                .height(o.bboxHeight())
                                .build())
                        .keypoints(o.keypoints().stream().map(this::toKeypointResponse).toList())
                        .labeledKeypointCount(
                            o.keypoints().stream().filter(k -> k.visibility() > 0).count())
                        .build())
            .toList();

    return PoseLabelResponse.builder()
        .labelId(null)
        .imageId(imageId)
        .imageWidth(snapshot.getImageWidth())
        .imageHeight(snapshot.getImageHeight())
        .status(AssignmentStatus.APPROVED)
        .editable(false)
        .objects(objects)
        .createdAt(snapshot.getCreatedAt())
        .updatedAt(snapshot.getCreatedAt())
        .build();
  }

  private KeypointResponse toKeypointResponse(SnapshotPayload.SnapshotKeypoint keypoint) {
    KeypointType type = KeypointType.valueOf(keypoint.type());
    Visibility visibility =
        switch (keypoint.visibility()) {
          case 1 -> Visibility.OCCLUDED;
          case 2 -> Visibility.VISIBLE;
          default -> Visibility.NOT_LABELED;
        };

    return KeypointResponse.builder()
        .type(type)
        .cocoIndex(keypoint.cocoIndex())
        .label(type.getKo())
        .group(type.getGroup())
        .x(keypoint.x())
        .y(keypoint.y())
        .visibility(visibility)
        .visibilityCode(keypoint.visibility())
        .build();
  }
}
