/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.signtelecom.datalabelbe.domain.image.dto.request.ImageBatchRequest;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageBatchResponse;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[관리자] 이미지 배치", description = "이미지 배치 관련 API")
@RequestMapping("/api/admin/image-batches")
public interface AdminImageBatchController {

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "[관리자] 이미지 배치 업로드",
      description =
          "배치를 새로 만들고 이미지 파일들을 한 번에 업로드합니다. 배치 이름을 비워두면 '2026-10-01 14:30 배치' 처럼 "
              + "현재 날짜와 시·분으로 자동 생성합니다. "
              + "용량(1건당 10MB)과 포맷(JPEG, PNG)은 클라이언트 검증을 신뢰하지 않고 서버에서 다시 확인하며, "
              + "검증에 실패한 파일은 통과한 파일의 저장을 막지 않고 응답의 failures에 사유와 함께 담깁니다.")
  ResponseEntity<BaseResponse<ImageUploadResponse>> createBatch(
      @Parameter(description = "배치 이름. 비우면 현재 날짜와 시·분으로 자동 생성")
          @RequestParam(value = "name", required = false)
          String name,
      @Parameter(description = "업로드할 이미지 파일 목록") @RequestPart(value = "files", required = false)
          List<MultipartFile> files);

  @GetMapping
  @Operation(
      summary = "[관리자] 이미지 배치 목록 조회",
      description = "전체 배치를 최신순으로 조회합니다. 배치마다 이미지 수, 할당 수, 승인 수, 검수대기 수를 함께 반환합니다.")
  ResponseEntity<BaseResponse<List<ImageBatchResponse>>> getBatches();

  @GetMapping("/{batchId}")
  @Operation(summary = "[관리자] 이미지 배치 상세 조회", description = "배치 하나의 상세 정보를 조회합니다.")
  ResponseEntity<BaseResponse<ImageBatchResponse>> getBatch(
      @Parameter(description = "배치 식별자") @PathVariable Long batchId);

  @PatchMapping("/{batchId}")
  @Operation(summary = "[관리자] 이미지 배치 이름 변경", description = "배치의 이름을 변경합니다.")
  ResponseEntity<BaseResponse<ImageBatchResponse>> updateBatch(
      @Parameter(description = "배치 식별자") @PathVariable Long batchId,
      @Parameter(description = "변경할 배치 정보") @RequestBody @Valid ImageBatchRequest request);

  @DeleteMapping("/{batchId}")
  @Operation(
      summary = "[관리자] 이미지 배치 삭제",
      description = "배치와 배치에 속한 모든 이미지를 삭제합니다. 저장된 실제 파일도 함께 정리됩니다. (Hard Delete)")
  ResponseEntity<BaseResponse<Void>> deleteBatch(
      @Parameter(description = "배치 식별자") @PathVariable Long batchId);
}
