/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.signtelecom.datalabelbe.domain.image.dto.request.ImageBatchRequest;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageBatchResponse;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadResponse;
import com.signtelecom.datalabelbe.domain.image.service.ImageBatchService;
import com.signtelecom.datalabelbe.domain.image.service.ImageService;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminImageBatchControllerImpl implements AdminImageBatchController {

  private final ImageBatchService imageBatchService;
  private final ImageService imageService;

  @Override
  public ResponseEntity<BaseResponse<ImageUploadResponse>> createBatch(
      String name, List<MultipartFile> files) {

    ImageUploadResponse response = imageService.createBatchAndUpload(name, files);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success(201, "이미지 배치가 생성되었습니다.", response));
  }

  @Override
  public ResponseEntity<BaseResponse<List<ImageBatchResponse>>> getBatches() {

    return ResponseEntity.ok(BaseResponse.success(imageBatchService.getBatches()));
  }

  @Override
  public ResponseEntity<BaseResponse<ImageBatchResponse>> getBatch(Long batchId) {

    return ResponseEntity.ok(BaseResponse.success(imageBatchService.getBatch(batchId)));
  }

  @Override
  public ResponseEntity<BaseResponse<ImageBatchResponse>> updateBatch(
      Long batchId, ImageBatchRequest request) {

    return ResponseEntity.ok(BaseResponse.success(imageBatchService.updateBatch(batchId, request)));
  }

  @Override
  public ResponseEntity<BaseResponse<Void>> deleteBatch(Long batchId) {

    imageBatchService.deleteBatch(batchId);

    return ResponseEntity.ok(BaseResponse.success());
  }
}
