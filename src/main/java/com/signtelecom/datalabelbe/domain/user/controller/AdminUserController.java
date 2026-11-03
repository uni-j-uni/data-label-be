/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.signtelecom.datalabelbe.domain.user.dto.response.UserDetailResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[관리자] 사용자", description = "사용자 관리 관리자 API")
@RequestMapping("/api/admin/users")
public interface AdminUserController {

  @GetMapping
  @Operation(
      summary = "[관리자] 사용자 전체 조회",
      description = "사용자 리스트 관리를 위해 일반 사용자(ROLE_USER) 계정을 전체 조회합니다. 관리자·개발자 계정은 결과에서 제외됩니다.")
  ResponseEntity<BaseResponse<List<UserDetailResponse>>> getAllUsers();
}
