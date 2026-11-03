/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.signtelecom.datalabelbe.domain.image.dto.response.ImageResponse;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[관리자] 이미지", description = "배치에 속한 이미지 업로드·목록·삭제 관리자 API")
@RequestMapping("/api/admin/image-batches/{batchId}/images")
public interface AdminImageController {

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "[관리자] 이미지 업로드",
      description =
          "이미 존재하는 배치에 이미지 파일을 추가로 업로드합니다. 용량(1건당 10MB)과 포맷(JPEG, PNG)을 서버에서 다시 검증하며, "
              + "실패한 파일은 통과한 파일의 저장을 막지 않고 응답의 failures에 사유와 함께 담깁니다.")
  ResponseEntity<BaseResponse<ImageUploadResponse>> uploadImages(
      @Parameter(description = "배치 식별자") @PathVariable Long batchId,
      @Parameter(description = "업로드할 이미지 파일 목록") @RequestPart(value = "files", required = false)
          List<MultipartFile> files);

  @GetMapping
  @Operation(
      summary = "[관리자] 이미지 목록 조회",
      description =
          "배치에 속한 이미지 목록을 작업 할당 현황과 함께 조회합니다. 미할당 이미지도 포함되며 그 경우 status는 PENDING(대기), "
              + "담당자 관련 필드는 null입니다. 관리자 할당 화면의 썸네일 그리드를 이 응답 하나로 그릴 수 있습니다.")
  ResponseEntity<BaseResponse<List<ImageResponse>>> getImages(
      @Parameter(description = "배치 식별자") @PathVariable Long batchId);

  @DeleteMapping("/{imageId}")
  @Operation(
      summary = "[관리자] 이미지 삭제",
      description =
          "배치에 속한 이미지 하나를 삭제합니다. (Hard Delete) 그 이미지의 작업 할당, 라벨, 제출 이력, 검수 코멘트, 승인 스냅샷과 "
              + "저장된 파일·썸네일 캐시가 함께 정리됩니다. 이미 고정된 데이터셋 버전은 COCO 사본이라 영향을 받지 않습니다.")
  ResponseEntity<BaseResponse<Void>> deleteImage(
      @Parameter(description = "배치 식별자") @PathVariable Long batchId,
      @Parameter(description = "이미지 식별자") @PathVariable Long imageId);
}
