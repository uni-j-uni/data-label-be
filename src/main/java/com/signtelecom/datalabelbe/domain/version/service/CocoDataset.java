/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.service;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * COCO keypoints 표준 포맷
 *
 * <p>필드 이름을 COCO 규격 그대로 맞춤. 별도 변환 스크립트 없이 기존 포즈 추정 학습 프레임워크에 바로 로드할 수 있어야 하기 때문
 *
 * @param info 생성 정보
 * @param images 이미지 목록
 * @param annotations 인물 객체 목록
 * @param categories 카테고리 정의 (person 하나)
 */
public record CocoDataset(
    CocoInfo info,
    List<CocoImage> images,
    List<CocoAnnotation> annotations,
    List<CocoCategory> categories) {

  /**
   * @param description 버전 이름
   * @param dateCreated 고정 일시 (ISO-8601)
   */
  public record CocoInfo(String description, @JsonProperty("date_created") String dateCreated) {}

  /**
   * @param id 이미지 식별자 (서버의 image.id 와 동일)
   * @param fileName 업로드 당시 원본 파일명
   * @param width 너비 (px)
   * @param height 높이 (px)
   */
  public record CocoImage(
      long id, @JsonProperty("file_name") String fileName, int width, int height) {}

  /**
   * @param id annotation 식별자 (버전 내에서 1부터)
   * @param imageId 대상 이미지 식별자
   * @param categoryId 카테고리 식별자 (person = 1)
   * @param bbox [x, y, width, height]
   * @param area 바운딩 박스 넓이
   * @param iscrowd 군집 여부. 인물 단위 라벨이므로 항상 0
   * @param numKeypoints 실제로 찍힌(v > 0) 키포인트 수
   * @param keypoints [x1, y1, v1, ... x17, y17, v17] 51개 값
   */
  public record CocoAnnotation(
      long id,
      @JsonProperty("image_id") long imageId,
      @JsonProperty("category_id") int categoryId,
      List<Double> bbox,
      double area,
      int iscrowd,
      @JsonProperty("num_keypoints") int numKeypoints,
      List<Double> keypoints) {}

  /**
   * @param id 카테고리 식별자
   * @param name 카테고리 이름
   * @param supercategory 상위 카테고리
   * @param keypoints COCO 순서의 키포인트 이름 17개
   * @param skeleton 키포인트 연결 정의 (1-based 쌍)
   */
  public record CocoCategory(
      int id,
      String name,
      String supercategory,
      List<String> keypoints,
      List<List<Integer>> skeleton) {}
}
