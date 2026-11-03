/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.signtelecom.datalabelbe.domain.user.entity.Role;
import com.signtelecom.datalabelbe.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

  /** 탈퇴하지 않은 계정만 조회 */
  Optional<User> findByUsernameAndDeletedAtIsNull(String username);

  /** 탈퇴하지 않은 계정만 조회 */
  Optional<User> findByIdAndDeletedAtIsNull(Long id);

  /** 탈퇴하지 않은 계정만 조회 */
  List<User> findByRoleAndDeletedAtIsNull(Role role);

  /** 탈퇴한 계정도 포함. 탈퇴한 아이디로 재가입하지 못하게 막는 데 사용 */
  boolean existsByUsername(String username);
}
