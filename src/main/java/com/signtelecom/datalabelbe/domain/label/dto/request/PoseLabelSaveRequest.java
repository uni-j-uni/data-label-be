/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    title = "PoseLabelSaveRequest DTO",
    description = "라벨 임시 저장 요청. 캔버스 전체 상태를 보내며 기존 객체는 전량 교체된다")
public class PoseLabelSaveRequest {

  @Valid
  @NotNull(message = "objects 항목은 필수입니다. 객체가 없으면 빈 배열을 보내세요.") @Schema(description = "인물 객체 목록. 모두 지운 상태를 저장하려면 빈 배열")
  private List<PoseObjectRequest> objects;
}
