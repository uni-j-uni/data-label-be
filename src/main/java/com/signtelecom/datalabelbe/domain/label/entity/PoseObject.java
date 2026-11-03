/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 이미지 안의 인물 한 명. 바운딩 박스와 17개 키포인트로 이뤄짐 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "pose_object",
    indexes = @Index(name = "idx_pose_object_pose_label_id", columnList = "pose_label_id"))
public class PoseObject {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pose_label_id", nullable = false)
  private PoseLabel label;

  /** 객체 표시 순서 (0부터) */
  @Column(name = "object_index", nullable = false)
  private Integer index;

  // 필드명 bboxX 를 기본 전략에 맡기면 컬럼이 bboxx 가 되어 bbox_width 와 어긋남. 이름 명시
  /** 바운딩 박스: 좌측 상단 x, y 와 너비, 높이 (픽셀) */
  @Column(name = "bbox_x", nullable = false)
  private Double bboxX;

  @Column(name = "bbox_y", nullable = false)
  private Double bboxY;

  @Column(name = "bbox_width", nullable = false)
  private Double bboxWidth;

  @Column(name = "bbox_height", nullable = false)
  private Double bboxHeight;

  /** 부모 삭제 시 함께 삭제 */
  @OneToMany(
      mappedBy = "poseObject",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("type asc")
  @Builder.Default
  private List<Keypoint> keypoints = new ArrayList<>();

  /** 부모 라벨을 연결. PoseLabel 이 자식을 추가할 때만 호출 */
  void assignTo(PoseLabel label) {
    this.label = label;
  }

  /** 키포인트를 추가하며 양방향 연관관계를 맞춤 */
  public void addKeypoint(Keypoint keypoint) {
    this.keypoints.add(keypoint);
    keypoint.assignTo(this);
  }

  /** 실제로 찍힌(visibility > 0) 키포인트 수 */
  public long labeledKeypointCount() {
    return keypoints.stream().filter(k -> k.getVisibility().isLabeled()).count();
  }
}
