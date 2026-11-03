/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.global.exception;

import com.signtelecom.datalabelbe.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class CustomException extends RuntimeException {

  private final BaseErrorCode errorCode;

  public CustomException(BaseErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  /** 하위 예외를 변환할 때 원인 예외를 함께 보존한다. */
  public CustomException(BaseErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.errorCode = errorCode;
    log.error(errorCode.getMessage(), errorCode, cause);
  }
}
