/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.exception;

import org.springframework.http.HttpStatus;

import com.signtelecom.datalabelbe.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {
  IMAGE_BATCH_NOT_FOUND("IMAGE4001", "이미지 배치를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  IMAGE_NOT_FOUND("IMAGE4002", "이미지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  EMPTY_FILE("IMAGE4003", "빈 파일은 업로드할 수 없습니다.", HttpStatus.BAD_REQUEST),
  UNSUPPORTED_CONTENT_TYPE("IMAGE4004", "JPEG 또는 PNG 이미지만 업로드할 수 있습니다.", HttpStatus.BAD_REQUEST),
  INVALID_IMAGE("IMAGE4005", "이미지를 읽을 수 없습니다. 파일이 손상되었을 수 있습니다.", HttpStatus.BAD_REQUEST),
  IMAGE_BATCH_FORBIDDEN("IMAGE4006", "해당 이미지 배치에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
  FILE_TOO_LARGE("IMAGE4007", "파일 용량 제한을 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE),
  NO_FILE_UPLOADED("IMAGE4008", "업로드할 파일이 없습니다.", HttpStatus.BAD_REQUEST),
  IMAGE_FILE_NOT_FOUND("IMAGE4009", "이미지 파일을 저장소에서 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_THUMBNAIL_WIDTH("IMAGE4010", "썸네일 너비는 16 이상 2048 이하여야 합니다.", HttpStatus.BAD_REQUEST),
  THUMBNAIL_FAILED("IMAGE5003", "썸네일 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  IMAGE_SAVE_FAILED("IMAGE5001", "이미지 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  IMAGE_DELETE_FAILED("IMAGE5002", "이미지 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
