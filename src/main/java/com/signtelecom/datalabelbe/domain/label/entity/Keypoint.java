/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 인물 객체 하나에 속한 키포인트. 객체마다 COCO 규격의 17개가 모두 존재 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "keypoint",
    indexes = @Index(name = "idx_keypoint_pose_object_id", columnList = "pose_object_id"))
public class Keypoint {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pose_object_id", nullable = false)
  private PoseObject poseObject;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private KeypointType type;

  /** 이미지 좌측 상단 기준 픽셀 좌표 */
  @Column(nullable = false)
  private Double x;

  @Column(nullable = false)
  private Double y;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private Visibility visibility;

  /** 부모 객체를 연결. PoseObject 가 자식을 추가할 때만 호출 */
  void assignTo(PoseObject poseObject) {
    this.poseObject = poseObject;
  }
}
