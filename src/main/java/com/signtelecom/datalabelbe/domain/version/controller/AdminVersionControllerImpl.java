/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.version.dto.request.VersionCreateRequest;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionDetailResponse;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionResponse;
import com.signtelecom.datalabelbe.domain.version.service.VersionService;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminVersionControllerImpl implements AdminVersionController {

  private final VersionService versionService;

  @Override
  public ResponseEntity<BaseResponse<VersionResponse>> createVersion(VersionCreateRequest request) {

    VersionResponse response = versionService.createVersion(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success(201, "데이터셋 버전이 고정되었습니다.", response));
  }

  @Override
  public ResponseEntity<BaseResponse<List<VersionResponse>>> getVersions() {

    return ResponseEntity.ok(BaseResponse.success(versionService.getVersions()));
  }

  @Override
  public ResponseEntity<BaseResponse<VersionDetailResponse>> getVersion(Long versionId) {

    return ResponseEntity.ok(BaseResponse.success(versionService.getVersion(versionId)));
  }

  @Override
  public ResponseEntity<BaseResponse<Void>> deleteVersion(Long versionId) {

    versionService.deleteVersion(versionId);

    return ResponseEntity.ok(BaseResponse.success());
  }

  @Override
  public ResponseEntity<String> exportCoco(Long versionId) {

    String coco = versionService.exportCoco(versionId);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(versionService.exportFilename(versionId), StandardCharsets.UTF_8)
                .build()
                .toString())
        .body(coco);
  }
}
