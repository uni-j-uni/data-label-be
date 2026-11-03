/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.signtelecom.datalabelbe.domain.image.entity.ImageBatch;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 승인된 라벨을 한 시점에 고정한 데이터셋 버전
 *
 * <p>생성 시점의 COCO JSON 전체를 컬럼에 저장. 이후 같은 배치에 이미지가 추가되거나 새로 승인돼도 이 버전의 내용은 바뀌지 않음
 *
 * <p>파일 대신 DB에 저장해 별도 파일 관리가 필요 없음. export 시 이 컬럼을 그대로 반환
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "dataset_version")
public class DatasetVersion extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "batch_id", nullable = false)
  private ImageBatch batch;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  /** 포함된 이미지 수. COCO images 배열의 길이와 같음 */
  @Column(nullable = false)
  private Integer imageCount;

  /** 포함된 annotation(인물 객체) 수 */
  @Column(nullable = false)
  private Integer annotationCount;

  /** 실제로 찍힌(visibility > 0) 키포인트 수 */
  @Column(nullable = false)
  private Integer labeledKeypointCount;

  /** 고정된 COCO keypoints 포맷 JSON 전문 */
  @Column(nullable = false, columnDefinition = "text")
  private String payload;
}
