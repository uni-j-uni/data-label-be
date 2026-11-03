/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 할당 이력의 변경 종류 */
@Getter
@RequiredArgsConstructor
@Schema(description = "할당 이력 변경 종류 Enum")
public enum AssignmentAction {
  @Schema(description = "미할당 이미지를 작업자에게 배정")
  ASSIGN("배정"),
  @Schema(description = "담당 작업자를 다른 작업자로 교체")
  REASSIGN("재배정"),
  @Schema(description = "할당 해제")
  RELEASE("해제");

  private final String ko;
}
