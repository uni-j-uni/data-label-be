/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.signtelecom.datalabelbe.domain.auth.dto.request.LoginRequest;
import com.signtelecom.datalabelbe.domain.auth.dto.response.TokenResponse;
import com.signtelecom.datalabelbe.domain.auth.exception.AuthErrorCode;
import com.signtelecom.datalabelbe.global.exception.CustomException;
import com.signtelecom.datalabelbe.global.exception.ErrorGuard;
import com.signtelecom.datalabelbe.global.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;

  @Override
  public TokenResponse login(LoginRequest loginRequest) {
    log.info("로그인 요청 시작 - username: {}", loginRequest.getUsername());
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(), loginRequest.getPassword());

    Authentication authenticated;
    try {
      authenticated = authenticationManager.authenticate(authenticationToken);
    } catch (BadCredentialsException e) {
      log.info("로그인 실패(자격 증명 불일치) - username: {}", loginRequest.getUsername());
      throw new CustomException(AuthErrorCode.LOGIN_FAIL, e);
    } catch (AuthenticationException e) {
      log.info("로그인 실패(인증 오류) - username: {}", loginRequest.getUsername());
      throw new CustomException(AuthErrorCode.LOGIN_FAIL, e);
    }

    TokenResponse tokenResponse = jwtProvider.createTokens(authenticated);
    log.info("로그인 성공 - username: {}", loginRequest.getUsername());
    return tokenResponse;
  }

  @Override
  public String logout(String accessToken) {
    log.info("로그아웃 요청 시작");
    String username = jwtProvider.getUsernameFromToken(accessToken);

    jwtProvider.deleteRefreshToken(username);
    jwtProvider.blacklistToken(accessToken);

    log.info("로그아웃 성공 - username: {}", username);
    return "로그아웃 성공 - 사용자: " + username;
  }

  @Override
  public String reissueAccessToken(String refreshToken) {
    log.info("AT 재발급 요청 시작");
    String username = jwtProvider.getUsernameFromToken(refreshToken);

    if (!jwtProvider.validateRefreshToken(username, refreshToken)) {
      log.info("AT 재발급 실패 - 유효하지 않은 refresh token - username: {}", username);
      ErrorGuard.fail(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    log.info("AT 재발급 성공 - username: {}", username);
    return jwtProvider.createAccessTokenFromRefreshToken(refreshToken);
  }
}
