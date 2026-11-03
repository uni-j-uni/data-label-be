/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.exception;

import org.springframework.http.HttpStatus;

import com.signtelecom.datalabelbe.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssignmentErrorCode implements BaseErrorCode {
  ASSIGNMENT_NOT_FOUND("ASGN4001", "작업 할당을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  WORKER_NOT_FOUND("ASGN4002", "작업자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  NOT_A_WORKER("ASGN4003", "일반 사용자(작업자) 계정만 작업을 할당받을 수 있습니다.", HttpStatus.BAD_REQUEST),
  NO_IMAGE_SELECTED("ASGN4005", "이미지를 하나 이상 선택해야 합니다.", HttpStatus.BAD_REQUEST),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
