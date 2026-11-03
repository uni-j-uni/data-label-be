/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.controller;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.image.dto.response.ImageResponse;
import com.signtelecom.datalabelbe.domain.image.service.ImageFile;
import com.signtelecom.datalabelbe.domain.image.service.ImageService;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ImageControllerImpl implements ImageController {

  private final ImageService imageService;

  @Override
  public ResponseEntity<BaseResponse<ImageResponse>> getImage(Long batchId, Long imageId) {

    return ResponseEntity.ok(BaseResponse.success(imageService.getImage(batchId, imageId)));
  }

  @Override
  public ResponseEntity<Resource> getImageFile(Long batchId, Long imageId, Integer w) {

    ImageFile file = imageService.loadImageFile(batchId, imageId, w);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(file.contentType()))
        .contentLength(file.sizeBytes())
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.inline()
                .filename(file.originalFilename(), StandardCharsets.UTF_8)
                .build()
                .toString())
        // 저장 파일명이 UUID라 내용이 바뀌는 일이 없으므로 길게 캐시해도 안전
        .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePrivate().immutable())
        .body(file.resource());
  }
}
