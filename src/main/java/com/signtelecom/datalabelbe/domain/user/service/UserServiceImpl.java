/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.signtelecom.datalabelbe.domain.assignment.entity.Assignment;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentAction;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentHistory;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.domain.assignment.mapper.AssignmentMapper;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentHistoryRepository;
import com.signtelecom.datalabelbe.domain.assignment.repository.AssignmentRepository;
import com.signtelecom.datalabelbe.domain.user.dto.request.SignUpRequest;
import com.signtelecom.datalabelbe.domain.user.dto.response.UserDetailResponse;
import com.signtelecom.datalabelbe.domain.user.dto.response.UserResponse;
import com.signtelecom.datalabelbe.domain.user.entity.Role;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.domain.user.exception.UserErrorCode;
import com.signtelecom.datalabelbe.domain.user.mapper.UserMapper;
import com.signtelecom.datalabelbe.domain.user.repository.UserRepository;
import com.signtelecom.datalabelbe.global.exception.CustomException;
import com.signtelecom.datalabelbe.global.exception.ErrorGuard;
import com.signtelecom.datalabelbe.global.jwt.JwtProvider;
import com.signtelecom.datalabelbe.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentHistoryRepository assignmentHistoryRepository;
  private final AssignmentMapper assignmentMapper;

  @Override
  @Transactional
  public UserResponse signUp(SignUpRequest request) {

    ErrorGuard.throwIf(
        userRepository.existsByUsername(request.getUsername()), UserErrorCode.EXIST_USERNAME);

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    User user = userMapper.toUser(request, encodedPassword);
    User savedUser;

    try {
      savedUser = userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException e) {
      // 사전 검사와 저장 사이에 동일 아이디가 선점된 경우
      log.info("회원가입 실패(아이디 중복) - username: {}", request.getUsername());
      return ErrorGuard.fail(UserErrorCode.EXIST_USERNAME, e);
    }

    log.info("회원가입 성공 - userId: {}, username: {}", savedUser.getId(), savedUser.getUsername());
    return userMapper.toUserResponse(savedUser);
  }

  @Override
  public List<UserDetailResponse> getAllUsers() {

    // 관리자·개발자 계정은 사용자 리스트 관리 대상이 아니므로 ROLE_USER 만 조회
    List<User> users = userRepository.findByRoleAndDeletedAtIsNull(Role.ROLE_USER);
    List<UserDetailResponse> userDetails =
        users.stream().map(userMapper::toUserDetailResponse).toList();

    log.info("일반 사용자 전체 조회, 총 사용자 수: {}", userDetails.size());
    return userDetails;
  }

  @Override
  public UserResponse getUserDetail() {

    User user = getCurrentUser();

    log.info("사용자 상세 조회 - userId: {}, username: {}", user.getId(), user.getUsername());
    return userMapper.toUserResponse(user);
  }

  @Transactional
  @Override
  public void deleteUser() {

    // withdraw() 가 변경 감지로 반영되도록 준영속인 현재 사용자 대신 영속 엔티티를 씀
    User user = userRepository.getReferenceById(getCurrentUser().getId());

    // 진행 중이거나 반려된 할당은 해제. 검수 대기·승인 건은 검수 흐름에 걸려 있으므로 유지
    List<Assignment> releasable =
        assignmentRepository.findByWorkerIdAndStatusIn(
            user.getId(), List.of(AssignmentStatus.IN_PROGRESS, AssignmentStatus.REJECTED));
    List<AssignmentHistory> histories =
        releasable.stream()
            .map(
                a ->
                    assignmentMapper.toHistory(
                        a.getImage(), AssignmentAction.RELEASE, user, null, user))
            .toList();
    assignmentRepository.deleteAll(releasable);
    assignmentHistoryRepository.saveAll(histories);

    user.withdraw();
    jwtProvider.deleteRefreshToken(user.getUsername());

    log.info(
        "회원 탈퇴 - userId: {}, username: {}, 해제된 할당: {}건",
        user.getId(),
        user.getUsername(),
        releasable.size());
  }

  @Override
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      log.error("인증 실패 - 인증 정보 없음");
      ErrorGuard.fail(UserErrorCode.UNAUTHORIZED);
    }

    Object principal = authentication.getPrincipal();

    // JwtAuthenticationFilter 가 요청마다 탈퇴 여부까지 확인하며 읽어 둔 사용자를 그대로 씀. 같은 요청에서 다시 조회하지 않기 위함
    // 이 엔티티는 준영속 상태라 식별자 비교·연관 참조에는 안전하지만, 필드를 바꿔도 DB 에 반영되지 않음.
    // 상태를 바꿔야 하면 deleteUser() 처럼 식별자로 영속 엔티티를 다시 받아 써야 함
    if (principal instanceof CustomUserDetails details) {
      return details.user();
    }

    String username;

    if (principal instanceof String str) {
      username = str;
    } else if (principal instanceof UserDetails userDetails) {
      username = userDetails.getUsername();
    } else {
      log.error("인증 실패 - Principal 타입 알 수 없음: {}", principal.getClass());
      return ErrorGuard.fail(UserErrorCode.UNAUTHORIZED);
    }

    if (username == null || username.isBlank()) {
      log.error("인증 실패 - 추출된 username이 null 또는 빈 문자열");
      ErrorGuard.fail(UserErrorCode.UNAUTHORIZED);
    }

    log.debug("JWT에서 추출한 사용자 아이디: {}", username);

    final String finalUsername = username;
    return userRepository
        .findByUsernameAndDeletedAtIsNull(finalUsername)
        .orElseThrow(
            () -> {
              log.error("사용자 찾기 실패 - username: {}", finalUsername);
              return new CustomException(UserErrorCode.USER_NOT_FOUND);
            });
  }
}
