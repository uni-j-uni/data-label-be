/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * COCO keypoints 표준의 17개 키포인트
 *
 * <p>{@link #getCocoIndex()} 순서는 COCO categories의 keypoints 배열 순서와 같아야 함. export 시 이 순서로 직렬화하므로 상수
 * 순서를 바꾸면 안 됨
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "COCO 17 keypoint 종류 Enum")
public enum KeypointType {
  NOSE(0, "코", KeypointGroup.FACE),
  LEFT_EYE(1, "왼쪽 눈", KeypointGroup.FACE),
  RIGHT_EYE(2, "오른쪽 눈", KeypointGroup.FACE),
  LEFT_EAR(3, "왼쪽 귀", KeypointGroup.FACE),
  RIGHT_EAR(4, "오른쪽 귀", KeypointGroup.FACE),
  LEFT_SHOULDER(5, "왼쪽 어깨", KeypointGroup.UPPER_BODY),
  RIGHT_SHOULDER(6, "오른쪽 어깨", KeypointGroup.UPPER_BODY),
  LEFT_ELBOW(7, "왼쪽 팔꿈치", KeypointGroup.UPPER_BODY),
  RIGHT_ELBOW(8, "오른쪽 팔꿈치", KeypointGroup.UPPER_BODY),
  LEFT_WRIST(9, "왼쪽 손목", KeypointGroup.UPPER_BODY),
  RIGHT_WRIST(10, "오른쪽 손목", KeypointGroup.UPPER_BODY),
  LEFT_HIP(11, "왼쪽 골반", KeypointGroup.LOWER_BODY),
  RIGHT_HIP(12, "오른쪽 골반", KeypointGroup.LOWER_BODY),
  LEFT_KNEE(13, "왼쪽 무릎", KeypointGroup.LOWER_BODY),
  RIGHT_KNEE(14, "오른쪽 무릎", KeypointGroup.LOWER_BODY),
  LEFT_ANKLE(15, "왼쪽 발목", KeypointGroup.LOWER_BODY),
  RIGHT_ANKLE(16, "오른쪽 발목", KeypointGroup.LOWER_BODY);

  /** COCO 규격이 고정한 키포인트 개수. 객체 하나는 반드시 이 개수를 채워야 함 */
  public static final int COUNT = 17;

  private final int cocoIndex;
  private final String ko;
  private final KeypointGroup group;
}
