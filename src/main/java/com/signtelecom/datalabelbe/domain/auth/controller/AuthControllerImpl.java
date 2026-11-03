/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.auth.dto.request.LoginRequest;
import com.signtelecom.datalabelbe.domain.auth.dto.response.TokenResponse;
import com.signtelecom.datalabelbe.domain.auth.exception.AuthErrorCode;
import com.signtelecom.datalabelbe.domain.auth.service.AuthService;
import com.signtelecom.datalabelbe.global.exception.ErrorGuard;
import com.signtelecom.datalabelbe.global.jwt.JwtProvider;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

  private final AuthService authService;
  private final JwtProvider jwtProvider;

  @Override
  public ResponseEntity<BaseResponse<String>> login(
      HttpServletResponse response, LoginRequest loginRequest) {

    TokenResponse tokenResponse = authService.login(loginRequest);

    jwtProvider.addJwtToCookie(
        response,
        tokenResponse.getAccessToken(),
        "ACCESS_TOKEN",
        jwtProvider.getExpirationTime(tokenResponse.getAccessToken()));

    jwtProvider.addJwtToCookie(
        response,
        tokenResponse.getRefreshToken(),
        "REFRESH_TOKEN",
        jwtProvider.getExpirationTime(tokenResponse.getRefreshToken()));

    return ResponseEntity.status(HttpStatus.OK)
        .body(BaseResponse.success(200, "로그인이 완료되었습니다.", tokenResponse.getUsername()));
  }

  @Override
  public ResponseEntity<BaseResponse<String>> logout(
      HttpServletRequest request, HttpServletResponse response) {

    String accessToken = jwtProvider.extractAccessToken(request);
    ErrorGuard.throwIf(accessToken == null, AuthErrorCode.ACCESS_TOKEN_NOT_FOUND);

    String result = authService.logout(accessToken);

    jwtProvider.removeJwtCookie(response, "ACCESS_TOKEN");
    jwtProvider.removeJwtCookie(response, "REFRESH_TOKEN");

    return ResponseEntity.status(HttpStatus.OK)
        .body(BaseResponse.success(200, "로그아웃이 완료되었습니다.", result));
  }

  @Override
  public ResponseEntity<BaseResponse<String>> reissueAccessToken(
      HttpServletRequest request, HttpServletResponse response) {

    String refreshToken = jwtProvider.extractRefreshToken(request);

    // 서명·만료 검증 먼저, 이후 타입 확인
    ErrorGuard.throwIf(
        !jwtProvider.validateToken(refreshToken), AuthErrorCode.INVALID_REFRESH_TOKEN);
    ErrorGuard.throwIf(
        !jwtProvider.validateTokenType(refreshToken, JwtProvider.TOKEN_TYPE_REFRESH),
        AuthErrorCode.INVALID_TOKEN_TYPE);

    String newAccessToken = authService.reissueAccessToken(refreshToken);

    jwtProvider.addJwtToCookie(
        response, newAccessToken, "ACCESS_TOKEN", jwtProvider.getExpirationTime(newAccessToken));

    return ResponseEntity.status(HttpStatus.OK)
        .body(BaseResponse.success(200, "액세스 토큰 재발급이 완료되었습니다.", null));
  }
}
