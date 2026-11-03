/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.mapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.signtelecom.datalabelbe.domain.label.entity.KeypointType;
import com.signtelecom.datalabelbe.domain.review.service.SnapshotPayload;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionImageResponse;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionResponse;
import com.signtelecom.datalabelbe.domain.version.entity.DatasetVersion;
import com.signtelecom.datalabelbe.domain.version.service.CocoDataset;

@Component
public class VersionMapper {

  /** 인물 카테고리 식별자. 이 시스템은 person 단일 카테고리만 다룸 */
  private static final int PERSON_CATEGORY_ID = 1;

  /** COCO person 표준 스켈레톤 (키포인트 1-based 인덱스 쌍) */
  private static final List<List<Integer>> PERSON_SKELETON =
      List.of(
          List.of(16, 14),
          List.of(14, 12),
          List.of(17, 15),
          List.of(15, 13),
          List.of(12, 13),
          List.of(6, 12),
          List.of(7, 13),
          List.of(6, 7),
          List.of(6, 8),
          List.of(7, 9),
          List.of(8, 10),
          List.of(9, 11),
          List.of(2, 3),
          List.of(1, 2),
          List.of(1, 3),
          List.of(2, 4),
          List.of(3, 5),
          List.of(4, 6),
          List.of(5, 7));

  /** COCO categories 의 keypoints 이름 배열. enum 상수 순서가 곧 COCO 순서 */
  public static final List<String> KEYPOINT_NAMES =
      Arrays.stream(KeypointType.values()).map(t -> t.name().toLowerCase(Locale.ROOT)).toList();

  /** person 카테고리 정의. 모든 버전에서 동일 */
  public CocoDataset.CocoCategory personCategory() {
    return new CocoDataset.CocoCategory(
        PERSON_CATEGORY_ID, "person", "person", KEYPOINT_NAMES, PERSON_SKELETON);
  }

  /** 이미지 메타데이터를 COCO images 항목으로 변환 */
  public CocoDataset.CocoImage toCocoImage(long imageId, String filename, int width, int height) {
    return new CocoDataset.CocoImage(imageId, filename, width, height);
  }

  /**
   * 스냅샷의 인물 객체 하나를 COCO annotation 으로 변환
   *
   * <p>키포인트는 [x1, y1, v1, ... x17, y17, v17] 51개 값의 1차원 배열로 변환. 스냅샷이 이미 COCO 순서로 정렬돼 있으므로 순서를 그대로 씀
   *
   * @param annotationId 버전 내 annotation 식별자
   * @param imageId 대상 이미지 식별자
   * @param object 스냅샷의 인물 객체
   * @return COCO annotation
   */
  public CocoDataset.CocoAnnotation toCocoAnnotation(
      long annotationId, long imageId, SnapshotPayload.SnapshotObject object) {

    List<Double> flattened = new ArrayList<>(KeypointType.COUNT * 3);
    int numKeypoints = 0;

    for (SnapshotPayload.SnapshotKeypoint keypoint : object.keypoints()) {
      if (keypoint.visibility() == 0) {
        // COCO 규약: v=0 은 '찍지 않은 점'이므로 좌표를 0 으로 비움
        // 에디터는 새 객체의 키포인트를 박스 안 템플릿 위치에 v=0 으로 깔아두는데, 작업자가 확인하지 않은
        // 그 템플릿 좌표가 학습 데이터에 섞이면 안 됨. DB 의 라벨에는 좌표를 그대로 남겨, 재작업 시
        // 에디터가 가이드 위치로 다시 쓸 수 있게 함
        flattened.add(0.0);
        flattened.add(0.0);
        flattened.add(0.0);
        continue;
      }

      flattened.add(keypoint.x());
      flattened.add(keypoint.y());
      flattened.add((double) keypoint.visibility());
      numKeypoints++;
    }

    return new CocoDataset.CocoAnnotation(
        annotationId,
        imageId,
        PERSON_CATEGORY_ID,
        List.of(object.bboxX(), object.bboxY(), object.bboxWidth(), object.bboxHeight()),
        object.bboxWidth() * object.bboxHeight(),
        0,
        numKeypoints,
        flattened);
  }

  /** 버전 엔티티를 요약 응답 DTO로 변환 */
  public VersionResponse toVersionResponse(DatasetVersion version) {
    return VersionResponse.builder()
        .versionId(version.getId())
        .name(version.getName())
        .batchId(version.getBatch().getId())
        .batchName(version.getBatch().getName())
        .imageCount(version.getImageCount())
        .annotationCount(version.getAnnotationCount())
        .labeledKeypointCount(version.getLabeledKeypointCount())
        .keypointsPerObject(KeypointType.COUNT)
        .createdByName(version.getCreatedBy().getName())
        .createdAt(version.getCreatedAt())
        .build();
  }

  /**
   * 고정된 COCO 에서 이미지별 상세 행을 뽑아냄
   *
   * <p>현재 DB가 아니라 저장된 COCO 에서 읽음
   *
   * @param dataset 고정된 COCO
   * @return 이미지별 상세 목록
   */
  public List<VersionImageResponse> toVersionImages(CocoDataset dataset) {
    return dataset.images().stream()
        .map(
            image -> {
              List<CocoDataset.CocoAnnotation> annotations =
                  dataset.annotations().stream().filter(a -> a.imageId() == image.id()).toList();

              int labeled =
                  annotations.stream().mapToInt(CocoDataset.CocoAnnotation::numKeypoints).sum();

              return VersionImageResponse.builder()
                  .imageId(image.id())
                  .filename(image.fileName())
                  .width(image.width())
                  .height(image.height())
                  .objectCount(annotations.size())
                  .labeledKeypointCount(labeled)
                  .totalKeypointCount(annotations.size() * KeypointType.COUNT)
                  .build();
            })
        .toList();
  }

  /** COCO info 항목을 만듦 */
  public CocoDataset.CocoInfo toCocoInfo(String versionName, java.time.LocalDateTime createdAt) {
    return new CocoDataset.CocoInfo(
        versionName, createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  }
}
