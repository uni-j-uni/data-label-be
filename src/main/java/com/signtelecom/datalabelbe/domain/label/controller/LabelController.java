/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.signtelecom.datalabelbe.domain.label.dto.request.PoseLabelSaveRequest;
import com.signtelecom.datalabelbe.domain.label.dto.response.PoseLabelResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "포즈 라벨", description = "COCO 17 keypoint 포즈 라벨 조회·저장 API")
@RequestMapping("/api/images/{imageId}/label")
public interface LabelController {

  @GetMapping
  @Operation(
      summary = "라벨 조회",
      description =
          "이미지의 포즈 라벨을 조회합니다. 저장된 적이 없으면 objects가 빈 배열로 내려가므로 클라이언트가 '라벨 없음'을 따로 처리할 필요가 없습니다. "
              + "배치 소유자·관리자·담당 작업자가 조회할 수 있으며, 검수 화면도 이 응답을 읽기 전용으로 사용합니다. "
              + "editable 필드로 수정 가능 여부를 판단하세요.")
  ResponseEntity<BaseResponse<PoseLabelResponse>> getLabel(
      @Parameter(description = "이미지 식별자") @PathVariable Long imageId);

  @PutMapping
  @Operation(
      summary = "라벨 임시 저장",
      description =
          "캔버스 전체 상태를 저장합니다. 기존 객체는 전량 교체되므로, 객체를 모두 지운 상태는 빈 배열로 보내세요. "
              + "담당 작업자만 저장할 수 있고 검수 대기·승인 완료 상태에서는 409로 거부합니다. "
              + "서버에서 키포인트 17종 여부·중복·좌표 범위를 다시 검증합니다.")
  ResponseEntity<BaseResponse<PoseLabelResponse>> saveLabel(
      @Parameter(description = "이미지 식별자") @PathVariable Long imageId,
      @Parameter(description = "인물 객체 목록") @RequestBody @Valid PoseLabelSaveRequest request);
}
