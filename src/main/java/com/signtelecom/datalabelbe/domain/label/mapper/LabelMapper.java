/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.mapper;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.label.dto.request.KeypointRequest;
import com.signtelecom.datalabelbe.domain.label.dto.request.PoseObjectRequest;
import com.signtelecom.datalabelbe.domain.label.dto.response.BoundingBoxResponse;
import com.signtelecom.datalabelbe.domain.label.dto.response.KeypointResponse;
import com.signtelecom.datalabelbe.domain.label.dto.response.PoseLabelResponse;
import com.signtelecom.datalabelbe.domain.label.dto.response.PoseObjectResponse;
import com.signtelecom.datalabelbe.domain.label.entity.Keypoint;
import com.signtelecom.datalabelbe.domain.label.entity.KeypointType;
import com.signtelecom.datalabelbe.domain.label.entity.PoseLabel;
import com.signtelecom.datalabelbe.domain.label.entity.PoseObject;

@Component
public class LabelMapper {

  /**
   * 요청 DTO를 PoseObject 엔티티로 변환
   *
   * <p>키포인트까지 함께 붙여 완성된 객체를 돌려줌. 부모 라벨 연결은 호출부에서 addObject로 처리
   *
   * @param request 인물 객체 요청 DTO
   * @param index 표시 순서 (0부터)
   * @return 신규 생성된 PoseObject 엔티티
   */
  public PoseObject toPoseObject(PoseObjectRequest request, int index) {
    PoseObject object =
        PoseObject.builder()
            .index(index)
            .bboxX(request.getBbox().getX())
            .bboxY(request.getBbox().getY())
            .bboxWidth(request.getBbox().getWidth())
            .bboxHeight(request.getBbox().getHeight())
            .build();

    request.getKeypoints().forEach(k -> object.addKeypoint(toKeypoint(k)));

    return object;
  }

  /** 요청 DTO를 Keypoint 엔티티로 변환 */
  public Keypoint toKeypoint(KeypointRequest request) {
    return Keypoint.builder()
        .type(request.getType())
        .x(request.getX())
        .y(request.getY())
        .visibility(request.getVisibility())
        .build();
  }

  /**
   * 라벨 엔티티를 응답 DTO로 변환
   *
   * <p>아직 저장된 적이 없는 이미지는 label이 null. 이 경우 빈 객체 목록으로 응답
   *
   * @param label 라벨 엔티티. 저장된 적이 없으면 null
   * @param image 대상 이미지
   * @param assignmentId 작업 할당 식별자. 할당이 없거나 검수 화면처럼 쓰지 않는 경우 null
   * @param status 현재 작업 상태
   * @param editable 수정 가능 여부
   * @return 라벨 응답 DTO
   */
  public PoseLabelResponse toPoseLabelResponse(
      PoseLabel label, Image image, Long assignmentId, AssignmentStatus status, boolean editable) {

    List<PoseObjectResponse> objects =
        label == null
            ? List.of()
            : label.getObjects().stream().map(this::toPoseObjectResponse).toList();

    return PoseLabelResponse.builder()
        .labelId(label == null ? null : label.getId())
        .imageId(image.getId())
        // 배치는 지연 로딩 프록시라 식별자만 읽으면 추가 조회가 없음
        .batchId(image.getBatch().getId())
        .assignmentId(assignmentId)
        .imageWidth(image.getWidth())
        .imageHeight(image.getHeight())
        .status(status)
        .editable(editable)
        .objects(objects)
        .createdAt(label == null ? null : label.getCreatedAt())
        .updatedAt(label == null ? null : label.getUpdatedAt())
        .build();
  }

  /** PoseObject 엔티티를 응답 DTO로 변환 */
  public PoseObjectResponse toPoseObjectResponse(PoseObject object) {
    // 클라이언트가 COCO 순서를 그대로 쓸 수 있도록 cocoIndex 순으로 정렬해 내려줌
    List<KeypointResponse> keypoints =
        object.getKeypoints().stream()
            .sorted(Comparator.comparingInt(k -> k.getType().getCocoIndex()))
            .map(this::toKeypointResponse)
            .toList();

    return PoseObjectResponse.builder()
        .objectId(object.getId())
        .index(object.getIndex())
        .bbox(
            BoundingBoxResponse.builder()
                .x(object.getBboxX())
                .y(object.getBboxY())
                .width(object.getBboxWidth())
                .height(object.getBboxHeight())
                .build())
        .keypoints(keypoints)
        .labeledKeypointCount(object.labeledKeypointCount())
        .build();
  }

  /** Keypoint 엔티티를 응답 DTO로 변환 */
  public KeypointResponse toKeypointResponse(Keypoint keypoint) {
    KeypointType type = keypoint.getType();

    return KeypointResponse.builder()
        .type(type)
        .cocoIndex(type.getCocoIndex())
        .label(type.getKo())
        .group(type.getGroup())
        .x(keypoint.getX())
        .y(keypoint.getY())
        .visibility(keypoint.getVisibility())
        .visibilityCode(keypoint.getVisibility().getCode())
        .build();
  }
}
