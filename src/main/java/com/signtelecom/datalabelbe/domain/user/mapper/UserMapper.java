/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.mapper;

import org.springframework.stereotype.Component;

import com.signtelecom.datalabelbe.domain.user.dto.request.SignUpRequest;
import com.signtelecom.datalabelbe.domain.user.dto.response.UserDetailResponse;
import com.signtelecom.datalabelbe.domain.user.dto.response.UserResponse;
import com.signtelecom.datalabelbe.domain.user.entity.Role;
import com.signtelecom.datalabelbe.domain.user.entity.User;

@Component
public class UserMapper {

  /**
   * 회원가입 요청 DTO와 인코딩된 비밀번호를 기반으로 User 엔티티를 생성
   *
   * <p>기본 역할(role)은 USER로 설정
   *
   * @param request 회원 정보 요청 DTO
   * @param encodedPassword 인코딩된 비밀번호
   * @return 신규 생성된 User 엔티티
   */
  public User toUser(SignUpRequest request, String encodedPassword) {
    return User.builder()
        .username(request.getUsername())
        .password(encodedPassword)
        .name(request.getName())
        .role(Role.ROLE_USER)
        .build();
  }

  /**
   * User 엔티티를 사용자 응답 DTO로 변환
   *
   * <p>role 포함
   *
   * @param user 사용자 엔티티
   * @return 사용자 응답 DTO
   */
  public UserResponse toUserResponse(User user) {
    return UserResponse.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .name(user.getName())
        .role(user.getRole().name())
        .build();
  }

  /**
   * User 엔티티를 사용자 응답 DTO로 변환
   *
   * @param user 사용자 엔티티
   * @return 사용자 응답 DTO
   */
  public UserDetailResponse toUserDetailResponse(User user) {
    return UserDetailResponse.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .name(user.getName())
        .role(user.getRole().toString())
        .build();
  }
}
