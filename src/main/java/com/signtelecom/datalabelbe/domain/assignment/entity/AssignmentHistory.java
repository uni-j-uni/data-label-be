/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 할당 변경 이력
 *
 * <p>배정·재배정·해제가 일어날 때마다 한 건씩 추가하고 수정하지 않음. 해제 시 Assignment 행은 삭제되므로 Assignment 대신 Image 를 참조
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "assignment_history",
    indexes = @Index(name = "idx_assignment_history_image_id", columnList = "image_id"))
public class AssignmentHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "image_id", nullable = false)
  private Image image;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private AssignmentAction action;

  /** 배정·재배정은 새 담당자, 해제는 해제된 담당자 */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "worker_id", nullable = false)
  private User worker;

  /** 재배정 전 담당자. 배정·해제 시에는 null */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "previous_worker_id")
  private User previousWorker;

  /** 변경을 수행한 관리자 */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "acted_by", nullable = false)
  private User actedBy;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
