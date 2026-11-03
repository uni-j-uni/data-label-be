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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검수 코멘트. 이미지 전체 또는 특정 인물 객체를 가리킴
 *
 * <p>대상 객체를 PoseObject 의 식별자가 아니라 표시 순서({@code targetObjectIndex})로 저장. 임시 저장 시 객체가 새로 만들어져 식별자가
 * 바뀌기 때문
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "comment",
    indexes = @Index(name = "idx_comment_submission_id", columnList = "submission_id"))
public class Comment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "submission_id", nullable = false)
  private Submission submission;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  /** 대상 인물의 표시 순서(0부터). null 이면 이미지 전체에 대한 코멘트 */
  @Column(name = "target_object_index")
  private Integer targetObjectIndex;

  @Column(nullable = false, length = 1000)
  private String text;

  /** 부모 제출 건을 연결. Submission 이 자식을 추가할 때만 호출 */
  void assignTo(Submission submission) {
    this.submission = submission;
  }
}
