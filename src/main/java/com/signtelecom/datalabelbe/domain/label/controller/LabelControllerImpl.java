/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.label.dto.request.PoseLabelSaveRequest;
import com.signtelecom.datalabelbe.domain.label.dto.response.PoseLabelResponse;
import com.signtelecom.datalabelbe.domain.label.service.LabelService;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LabelControllerImpl implements LabelController {

  private final LabelService labelService;

  @Override
  public ResponseEntity<BaseResponse<PoseLabelResponse>> getLabel(Long imageId) {

    return ResponseEntity.ok(BaseResponse.success(labelService.getLabel(imageId)));
  }

  @Override
  public ResponseEntity<BaseResponse<PoseLabelResponse>> saveLabel(
      Long imageId, PoseLabelSaveRequest request) {

    return ResponseEntity.ok(
        BaseResponse.success(200, "라벨이 저장되었습니다.", labelService.saveLabel(imageId, request)));
  }
}
