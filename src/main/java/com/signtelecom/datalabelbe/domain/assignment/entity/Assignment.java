/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 이미지 한 장을 작업자 한 명에게 배정한 기록. 이미지당 최대 하나만 존재 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "assignment",
    indexes = @Index(name = "idx_assignment_worker_id", columnList = "worker_id"))
public class Assignment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "image_id", nullable = false, unique = true)
  private Image image;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "worker_id", nullable = false)
  private User worker;

  /** 최초 배정한 관리자. 재배정 시에는 바뀌지 않으며 변경 내역은 AssignmentHistory 에 기록 */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "assigned_by", nullable = false)
  private User assignedBy;

  /** 할당이 생기는 순간부터 작업이 시작된 것으로 봄. PENDING은 미할당을 뜻하므로 여기에 저장하지 않음 */
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private AssignmentStatus status = AssignmentStatus.IN_PROGRESS;

  /** 주어진 사용자가 이 할당의 담당 작업자인지 확인 */
  public boolean isAssignedTo(User user) {
    return this.worker.getId().equals(user.getId());
  }

  /** 승인 완료된 할당인지 확인. 승인된 할당은 재할당·해제 불가 */
  public boolean isApproved() {
    return this.status == AssignmentStatus.APPROVED;
  }

  /**
   * 담당 작업자만 교체. assignedBy 는 유지
   *
   * <p>라벨 데이터는 이미지에 속하므로 담당자가 바뀌어도 진행 상태를 유지해, 기존 작업을 이어서 할 수 있게 함
   */
  public void reassignTo(User worker) {
    this.worker = worker;
  }

  /** 할당 상태를 변경 */
  public void changeStatus(AssignmentStatus status) {
    this.status = status;
  }
}
