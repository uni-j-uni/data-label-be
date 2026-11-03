/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.signtelecom.datalabelbe.domain.image.dto.response.ImageResponse;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadResponse;
import com.signtelecom.datalabelbe.domain.image.service.ImageService;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminImageControllerImpl implements AdminImageController {

  private final ImageService imageService;

  @Override
  public ResponseEntity<BaseResponse<ImageUploadResponse>> uploadImages(
      Long batchId, List<MultipartFile> files) {

    ImageUploadResponse response = imageService.uploadImages(batchId, files);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success(201, "이미지 업로드가 완료되었습니다.", response));
  }

  @Override
  public ResponseEntity<BaseResponse<List<ImageResponse>>> getImages(Long batchId) {

    return ResponseEntity.ok(BaseResponse.success(imageService.getImages(batchId)));
  }

  @Override
  public ResponseEntity<BaseResponse<Void>> deleteImage(Long batchId, Long imageId) {

    imageService.deleteImage(batchId, imageId);

    return ResponseEntity.ok(BaseResponse.success());
  }
}
