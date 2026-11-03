/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.exception;

import org.springframework.http.HttpStatus;

import com.signtelecom.datalabelbe.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VersionErrorCode implements BaseErrorCode {
  VERSION_NOT_FOUND("VER4001", "데이터셋 버전을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  NO_APPROVED_LABEL("VER4002", "승인 완료된 라벨이 없어 버전을 만들 수 없습니다.", HttpStatus.BAD_REQUEST),

  COCO_SERIALIZE_FAILED("VER5001", "COCO JSON 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
