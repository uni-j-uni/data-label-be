/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.global.response;

import com.signtelecom.datalabelbe.global.exception.model.BaseErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "BaseResponse DTO", description = "공통 API 응답 형식")
public class BaseResponse<T> {

  @Schema(description = "요청 성공 여부", example = "true")
  private boolean success;

  @Schema(description = "HTTP 상태 코드", example = "200")
  private int code;

  @Schema(description = "업무 에러 코드 (성공 시 미포함)", example = "AUTH4005")
  private String errorCode;

  @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
  private String message;

  @Schema(description = "응답 데이터")
  private T data;

  public static <T> BaseResponse<T> success() {
    return success(null);
  }

  public static <T> BaseResponse<T> success(T data) {
    return new BaseResponse<>(true, 200, null, "요청이 성공적으로 처리되었습니다.", data);
  }

  public static <T> BaseResponse<T> success(int code, String message, T data) {
    return new BaseResponse<>(true, code, null, message, data);
  }

  /** BaseErrorCode 하나로 status/code/message를 한 번에 채움 */
  public static <T> BaseResponse<T> error(BaseErrorCode errorCode) {
    return new BaseResponse<>(
        false, errorCode.getStatus().value(), errorCode.getCode(), errorCode.getMessage(), null);
  }

  /** 메시지만 덮어쓰는 경우 (검증 실패 상세 등) */
  public static <T> BaseResponse<T> error(BaseErrorCode errorCode, String message) {
    return new BaseResponse<>(
        false, errorCode.getStatus().value(), errorCode.getCode(), message, null);
  }
}
