/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.signtelecom.datalabelbe.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 승인 시점의 라벨 복사본
 *
 * <p>PoseLabel 은 승인 후에도 재작업으로 수정될 수 있으므로, 승인 당시 내용을 따로 복사해 둠. 생성 후에는 수정하지 않음
 *
 * <p>객체·키포인트를 별도 테이블 대신 JSON 하나로 저장. 수정 없이 전체를 읽어 COCO 로 변환하는 용도라 JSON 으로 충분
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "submission_snapshot")
public class SubmissionSnapshot extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "submission_id", nullable = false, unique = true)
  private Submission submission;

  /** 승인 시점의 이미지 해상도. COCO images 항목에 그대로 사용 */
  @Column(nullable = false)
  private Integer imageWidth;

  @Column(nullable = false)
  private Integer imageHeight;

  @Column(nullable = false)
  private Integer objectCount;

  @Column(nullable = false)
  private Integer labeledKeypointCount;

  /** 객체와 키포인트를 복사한 JSON. {@code SnapshotPayload} 구조와 대응 */
  @Column(nullable = false, columnDefinition = "text")
  private String payload;
}
