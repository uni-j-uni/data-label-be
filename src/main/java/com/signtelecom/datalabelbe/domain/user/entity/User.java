/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.signtelecom.datalabelbe.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, length = 20)
  private String name;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Role role = Role.ROLE_USER;

  /** 탈퇴 일시. null 이면 활성 계정. 작업 이력이 이 행을 참조하므로 탈퇴 시 행을 지우지 않음 */
  private LocalDateTime deletedAt;

  /** 탈퇴한 계정인지 확인 */
  public boolean isDeleted() {
    return this.deletedAt != null;
  }

  /** 탈퇴 처리 */
  public void withdraw() {
    this.deletedAt = LocalDateTime.now();
  }
}
