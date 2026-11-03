/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이미지 한 장의 포즈 라벨. 이미지당 하나만 존재
 *
 * <p>승인 후에도 반려·재작업 시 이 행을 계속 덮어씀. 승인 당시 데이터는 SubmissionSnapshot 에 따로 복사해 둠
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "pose_label")
public class PoseLabel extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "image_id", nullable = false, unique = true)
  private Image image;

  @OneToMany(
      mappedBy = "label",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("index asc")
  @Builder.Default
  private List<PoseObject> objects = new ArrayList<>();

  /**
   * 기존 객체를 모두 지우고 새 목록으로 교체
   *
   * <p>임시 저장 요청에 전체 객체가 담겨 오므로 부분 수정 없이 전부 교체. 임시 저장은 기존 객체와 키포인트를 벌크 delete 로 먼저 지운 뒤
   * 호출하므로(LabelServiceImpl.saveLabel), 여기서는 orphanRemoval 이 지울 행이 없음
   */
  public void replaceObjects(List<PoseObject> newObjects) {
    this.objects.clear();
    newObjects.forEach(this::addObject);
  }

  /** 객체를 추가하며 양방향 연관관계를 맞춤 */
  public void addObject(PoseObject object) {
    this.objects.add(object);
    object.assignTo(this);
  }
}
