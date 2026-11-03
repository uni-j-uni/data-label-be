/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.global.exception;

import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.signtelecom.datalabelbe.global.exception.model.BaseErrorCode;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리 클래스
 *
 * <p>Spring Controller에서 발생하는 예외를 처리하고, 클라이언트에게 일관된 형태의 {@link BaseResponse}를 반환
 *
 * <p>처리 범위:
 *
 * <ul>
 *   <li>커스텀 예외 (CustomException)
 *   <li>유효성 검증 실패 (MethodArgumentNotValidException)
 *   <li>정적 리소스 미존재 (NoResourceFoundException)
 *   <li>기타 예상치 못한 예외 (Exception)
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex) {
    BaseErrorCode errorCode = ex.getErrorCode();
    log.warn("CustomException 발생: {} - {}", errorCode.getCode(), errorCode.getMessage());
    return ResponseEntity.status(errorCode.getStatus()).body(BaseResponse.error(errorCode));
  }

  /**
   * @Valid 검증 실패 처리
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String errorMessages =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining(" / "));
    log.warn("Validation 오류 발생: {}", errorMessages);
    return ResponseEntity.badRequest()
        .body(BaseResponse.error(GlobalErrorCode.INVALID_INPUT_VALUE, errorMessages));
  }

  /**
   * @RequestParam/@PathVariable 검증 실패 (@Validated 사용 시)
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<BaseResponse<Object>> handleConstraintViolation(
      ConstraintViolationException ex) {
    String errorMessages =
        ex.getConstraintViolations().stream()
            .map(v -> String.format("[%s] %s", v.getPropertyPath(), v.getMessage()))
            .collect(Collectors.joining(" / "));
    log.warn("파라미터 검증 오류: {}", errorMessages);
    return ResponseEntity.badRequest()
        .body(BaseResponse.error(GlobalErrorCode.INVALID_INPUT_VALUE, errorMessages));
  }

  /** id 타입이 안 맞는 경우 */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<BaseResponse<Object>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    log.warn("타입 불일치: {} = {}", ex.getName(), ex.getValue());
    return ResponseEntity.badRequest()
        .body(
            BaseResponse.error(
                GlobalErrorCode.INVALID_INPUT_VALUE,
                String.format("[%s] 파라미터 형식이 올바르지 않습니다.", ex.getName())));
  }

  /**
   * @PreAuthorize 도입 시 403이 500으로 뒤바뀌는 것 방지
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<BaseResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
    log.warn("권한 부족(메서드 보안): {}", ex.getMessage());
    return ResponseEntity.status(GlobalErrorCode.ACCESS_DENIED.getStatus())
        .body(BaseResponse.error(GlobalErrorCode.ACCESS_DENIED));
  }

  /**
   * 부모가 처리하는 나머지 MVC 표준 예외(400/404/405/406/413/415...)의 본문을 BaseResponse로 통일
   *
   * <p>413은 서블릿 multipart 제한({@code spring.servlet.multipart.max-file-size})을 넘은 경우다. 이 단계에서 걸리면 요청
   * 전체가 거부되어 파일별 실패 리포팅을 할 수 없으므로, 서블릿 제한은 앱 용량 제한({@code app.file.max-size-bytes})보다 높게 두고 있음
   */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    log.warn("{} 발생: {}", ex.getClass().getSimpleName(), ex.getMessage());
    BaseErrorCode errorCode =
        switch (statusCode.value()) {
          case 404 -> GlobalErrorCode.RESOURCE_NOT_FOUND;
          case 413 -> GlobalErrorCode.PAYLOAD_TOO_LARGE;
          default -> statusCode.is4xxClientError()
              ? GlobalErrorCode.INVALID_INPUT_VALUE
              : GlobalErrorCode.INTERNAL_SERVER_ERROR;
        };
    return ResponseEntity.status(statusCode)
        .headers(headers)
        .body(
            new BaseResponse<>(
                false, statusCode.value(), errorCode.getCode(), errorCode.getMessage(), null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
    log.error("Server 오류 발생", ex);
    return ResponseEntity.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(BaseResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR));
  }
}
