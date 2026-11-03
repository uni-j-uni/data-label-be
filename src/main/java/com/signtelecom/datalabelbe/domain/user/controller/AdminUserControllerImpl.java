/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.user.dto.response.UserDetailResponse;
import com.signtelecom.datalabelbe.domain.user.service.UserService;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminUserControllerImpl implements AdminUserController {

  private final UserService userService;

  @Override
  public ResponseEntity<BaseResponse<List<UserDetailResponse>>> getAllUsers() {

    return ResponseEntity.ok(BaseResponse.success(userService.getAllUsers()));
  }
}
