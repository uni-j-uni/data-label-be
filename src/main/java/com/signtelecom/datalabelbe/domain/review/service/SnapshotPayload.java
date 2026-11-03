/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.service;

import java.util.List;

/**
 * 승인 스냅샷의 JSON 구조
 *
 * <p>COCO export 시 바로 쓸 수 있도록 키포인트의 cocoIndex 와 visibility 정수 코드를 그대로 저장. enum 이 바뀌어도 기존 스냅샷에는 영향
 * 없음
 *
 * @param objects 승인 시점의 인물 객체 목록
 */
public record SnapshotPayload(List<SnapshotObject> objects) {

  /**
   * @param index 표시 순서 (0부터)
   * @param bboxX 바운딩 박스 좌측 상단 x
   * @param bboxY 바운딩 박스 좌측 상단 y
   * @param bboxWidth 바운딩 박스 너비
   * @param bboxHeight 바운딩 박스 높이
   * @param keypoints COCO 순서로 정렬된 키포인트 17개
   */
  public record SnapshotObject(
      int index,
      double bboxX,
      double bboxY,
      double bboxWidth,
      double bboxHeight,
      List<SnapshotKeypoint> keypoints) {}

  /**
   * @param type 키포인트 종류 이름
   * @param cocoIndex COCO keypoints 배열 순서
   * @param x x 좌표
   * @param y y 좌표
   * @param visibility COCO v 값 (0/1/2)
   */
  public record SnapshotKeypoint(String type, int cocoIndex, double x, double y, int visibility) {}
}
