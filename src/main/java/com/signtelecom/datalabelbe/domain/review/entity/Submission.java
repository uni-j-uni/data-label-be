/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 작업자가 검수를 요청한 이력 한 건
 *
 * <p>반려 후 재제출하면 새 제출 건이 쌓이므로, 이미지 하나에 여러 제출이 존재할 수 있음
 *
 * <p>Assignment 를 참조하지 않고 Image 를 참조. 관리자가 할당을 해제해도 제출 이력은 남아야 하기 때문
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "submission",
    indexes = {
      @Index(name = "idx_submission_image_id", columnList = "image_id"),
      @Index(name = "idx_submission_status_created_at", columnList = "status, created_at")
    })
public class Submission extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "image_id", nullable = false)
  private Image image;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "submitted_by", nullable = false)
  private User submittedBy;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private SubmissionStatus status = SubmissionStatus.PENDING_REVIEW;

  /** 제출 당시의 객체 수 */
  @Column(nullable = false)
  private Integer objectCount;

  /** 제출 당시의 찍힌 키포인트 수 */
  @Column(nullable = false)
  private Integer labeledKeypointCount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by")
  private User reviewedBy;

  private LocalDateTime reviewedAt;

  /** 제출 건 삭제 시 함께 삭제 */
  @OneToMany(
      mappedBy = "submission",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("id asc")
  @Builder.Default
  private List<Comment> comments = new ArrayList<>();

  // 승인 스냅샷은 여기에 연관을 두지 않고 SubmissionSnapshotRepository 로 조회함 (사유는 해당 리포지토리 참고)

  /** 검수 대기 상태인지 확인. 이 상태에서만 승인·반려할 수 있음 */
  public boolean isPendingReview() {
    return this.status == SubmissionStatus.PENDING_REVIEW;
  }

  /** 주어진 사용자가 이 건의 제출자인지 확인 */
  public boolean isSubmittedBy(User user) {
    return this.submittedBy.getId().equals(user.getId());
  }

  /** 검수 결과를 기록 */
  public void review(SubmissionStatus status, User reviewer, LocalDateTime reviewedAt) {
    this.status = status;
    this.reviewedBy = reviewer;
    this.reviewedAt = reviewedAt;
  }

  /** 코멘트를 추가하며 양방향 연관관계를 맞춤 */
  public void addComment(Comment comment) {
    this.comments.add(comment);
    comment.assignTo(this);
  }
}
