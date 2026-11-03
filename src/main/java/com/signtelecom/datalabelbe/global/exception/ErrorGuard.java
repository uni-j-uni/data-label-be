/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.global.exception;

import com.signtelecom.datalabelbe.global.exception.model.BaseErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 비즈니스 오류 발생 지점을 한곳으로 모은 유틸리티
 *
 * <p>여기서 던진 {@link CustomException}은 {@link GlobalExceptionHandler}에서 일괄 수신하여 에러 코드에 맞는 응답으로 변환한다.
 */
@Slf4j
public final class ErrorGuard {

  private ErrorGuard() {}

  /** 조건이 참이면 해당 에러 코드로 예외를 던진다. */
  public static void throwIf(boolean condition, BaseErrorCode errorCode) {
    if (condition) {
      fail(errorCode);
    }
  }

  /**
   * 해당 에러 코드로 예외를 던진다.
   *
   * <p>항상 예외를 던지므로 반환하지 않는다. 반환 타입은 값을 돌려줘야 하는 메서드에서 {@code return ErrorGuard.fail(...)}로 쓰기 위함이다.
   */
  public static <T> T fail(BaseErrorCode errorCode) {
    throw new CustomException(errorCode);
  }

  /** 원인 예외를 보존한 채 해당 에러 코드로 예외를 던진다. */
  public static <T> T fail(BaseErrorCode errorCode, Throwable cause) {
    throw new CustomException(errorCode, cause);
  }
}
