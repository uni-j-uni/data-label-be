/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.exception;

import org.springframework.http.HttpStatus;

import com.signtelecom.datalabelbe.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {
  SUBMISSION_NOT_FOUND("REVIEW4001", "제출 건을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  ALREADY_REVIEWED("REVIEW4002", "이미 검수가 끝난 제출 건입니다.", HttpStatus.CONFLICT),
  REJECT_REQUIRES_COMMENT("REVIEW4003", "반려하려면 사유 코멘트를 하나 이상 남겨야 합니다.", HttpStatus.BAD_REQUEST),
  SUBMISSION_FORBIDDEN("REVIEW4004", "해당 제출 건에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
  INVALID_TARGET_OBJECT("REVIEW4005", "코멘트 대상 객체를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
  ASSIGNMENT_NOT_FOUND_FOR_SUBMISSION(
      "REVIEW4006", "제출 건에 연결된 작업 할당이 없어 검수를 처리할 수 없습니다.", HttpStatus.CONFLICT),

  SNAPSHOT_SERIALIZE_FAILED("REVIEW5001", "승인 스냅샷 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
