/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.signtelecom.datalabelbe.domain.user.dto.request.SignUpRequest;
import com.signtelecom.datalabelbe.domain.user.dto.response.UserResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "사용자", description = "회원가입·내 정보·탈퇴 API")
@RequestMapping("/api/users")
public interface UserController {

  @PostMapping("sign-up")
  @Operation(summary = "회원가입", description = "사용자 회원가입을 처리합니다.")
  ResponseEntity<BaseResponse<UserResponse>> signUp(
      @Parameter(description = "회원가입 정보") @RequestBody @Valid SignUpRequest signUpRequest);

  @GetMapping
  @Operation(summary = "사용자 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
  ResponseEntity<BaseResponse<UserResponse>> getUserDetail();

  @DeleteMapping
  @Operation(
      summary = "회원 탈퇴",
      description = "현재 로그인한 사용자를 탈퇴 처리합니다. (Soft Delete: 계정 행은 남고 로그인 불가, 진행 중인 할당은 해제)")
  ResponseEntity<BaseResponse<Void>> deleteUser(HttpServletResponse response);
}
