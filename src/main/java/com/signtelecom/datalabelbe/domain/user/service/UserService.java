/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.service;

import java.util.List;

import com.signtelecom.datalabelbe.domain.user.dto.request.SignUpRequest;
import com.signtelecom.datalabelbe.domain.user.dto.response.UserDetailResponse;
import com.signtelecom.datalabelbe.domain.user.dto.response.UserResponse;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.global.exception.CustomException;

/** 회원 가입, 조회, 탈퇴 등 사용자 비즈니스 로직을 처리하는 서비스 */
public interface UserService {

  /**
   * 회원가입 요청을 처리
   *
   * <p>회원가입 시 중복된 아이디가 존재하면 예외를 발생시키며, 비밀번호는 인코딩되어 저장됨
   *
   * @param request 회원가입 요청 데이터 (아이디, 비밀번호, 닉네임 등)
   * @return 회원가입 성공 시 생성된 사용자 정보를 담은 응답 DTO
   * @throws CustomException 중복된 아이디 등 가입 불가 사유 발생 시
   */
  UserResponse signUp(SignUpRequest request);

  /**
   * 일반 사용자(ROLE_USER) 목록을 조회
   *
   * <p>사용자 리스트 관리를 위한 관리자 전용 조회. 관리자·개발자 계정은 관리 대상이 아니므로 결과에서 제외
   *
   * @return 일반 사용자 상세 정보 목록
   * @throws CustomException 사용자 조회 과정에서 오류가 발생한 경우
   */
  List<UserDetailResponse> getAllUsers();

  /**
   * 현재 인증된 사용자의 상세 정보를 조회
   *
   * <p>인증 컨텍스트에 저장된 사용자 정보를 기반으로 요청을 수행한 사용자의 상세 정보를 반환
   *
   * @return 현재 로그인한 사용자의 상세 정보
   * @throws CustomException 인증 정보가 없거나 사용자를 찾을 수 없는 경우
   */
  UserResponse getUserDetail();

  /**
   * 현재 인증된 사용자의 회원 탈퇴를 처리
   *
   * <p>행은 남기고 deletedAt 만 채움. 이후 로그인·토큰 인증·재가입 불가. 진행 중(IN_PROGRESS)·반려(REJECTED) 할당은 해제하고 RELEASE
   * 이력을 남김
   *
   * @throws CustomException 인증 정보가 없거나 탈퇴 처리 중 오류가 발생한 경우
   */
  void deleteUser();

  /**
   * 현재 인증된 사용자 엔티티를 조회
   *
   * <p>인증 컨텍스트에서 아이디를 추출해 사용자를 조회. 다른 도메인에서 리소스 소유자를 확인할 때 사용
   *
   * @return 현재 로그인한 사용자 엔티티
   * @throws CustomException 인증 정보가 없거나 사용자를 찾을 수 없는 경우
   */
  User getCurrentUser();
}
