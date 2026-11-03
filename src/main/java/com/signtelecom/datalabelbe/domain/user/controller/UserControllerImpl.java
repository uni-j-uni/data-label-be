/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.user.dto.request.SignUpRequest;
import com.signtelecom.datalabelbe.domain.user.dto.response.UserResponse;
import com.signtelecom.datalabelbe.domain.user.service.UserService;
import com.signtelecom.datalabelbe.global.jwt.JwtProvider;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

  private final UserService userService;
  private final JwtProvider jwtProvider;

  @Override
  public ResponseEntity<BaseResponse<UserResponse>> signUp(SignUpRequest request) {

    UserResponse response = userService.signUp(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success(201, "회원가입이 완료되었습니다.", response));
  }

  @Override
  public ResponseEntity<BaseResponse<UserResponse>> getUserDetail() {

    return ResponseEntity.ok(BaseResponse.success(userService.getUserDetail()));
  }

  @Override
  public ResponseEntity<BaseResponse<Void>> deleteUser(HttpServletResponse response) {

    userService.deleteUser();
    jwtProvider.removeJwtCookie(response, JwtProvider.TOKEN_TYPE_ACCESS);
    jwtProvider.removeJwtCookie(response, JwtProvider.TOKEN_TYPE_REFRESH);

    return ResponseEntity.ok(BaseResponse.success());
  }
}
