/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.exception;

import org.springframework.http.HttpStatus;

import com.signtelecom.datalabelbe.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LabelErrorCode implements BaseErrorCode {
  LABEL_NOT_FOUND("LABEL4001", "라벨을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  NOT_ASSIGNED_WORKER("LABEL4002", "본인에게 할당된 이미지만 라벨링할 수 있습니다.", HttpStatus.FORBIDDEN),
  NOT_EDITABLE("LABEL4003", "검수 대기 또는 승인 완료된 작업은 수정할 수 없습니다.", HttpStatus.CONFLICT),
  INVALID_KEYPOINT_COUNT("LABEL4004", "키포인트는 객체마다 정확히 17개여야 합니다.", HttpStatus.BAD_REQUEST),
  DUPLICATE_KEYPOINT_TYPE("LABEL4005", "같은 종류의 키포인트가 중복되었습니다.", HttpStatus.BAD_REQUEST),
  COORDINATE_OUT_OF_BOUNDS("LABEL4006", "좌표가 이미지 범위를 벗어났습니다.", HttpStatus.BAD_REQUEST),
  INVALID_BBOX("LABEL4007", "바운딩 박스의 너비와 높이는 0보다 커야 합니다.", HttpStatus.BAD_REQUEST),
  NO_OBJECT_TO_SUBMIT("LABEL4008", "인물 객체가 하나도 없어 검수 요청할 수 없습니다.", HttpStatus.BAD_REQUEST),
  ALREADY_SUBMITTED("LABEL4009", "이미 검수 요청된 작업입니다.", HttpStatus.CONFLICT),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
