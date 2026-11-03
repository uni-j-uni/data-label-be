/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.signtelecom.datalabelbe.domain.version.dto.request.VersionCreateRequest;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionDetailResponse;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[관리자] 버전 · Export", description = "승인본 버전 고정과 COCO keypoints 내보내기 API")
@RequestMapping("/api/admin/versions")
public interface AdminVersionController {

  @PostMapping
  @Operation(
      summary = "[관리자] 현재 승인본으로 버전 생성",
      description =
          "배치의 승인 완료된 라벨을 모아 COCO JSON 을 만들고 통째로 고정합니다. 이후 원본 라벨이 수정되거나 이미지가 추가돼도 "
              + "이미 만든 버전의 내보내기 결과는 변하지 않습니다. 이름을 비우면 v1, v2 ... 로 자동 생성합니다.")
  ResponseEntity<BaseResponse<VersionResponse>> createVersion(
      @Parameter(description = "버전 생성 정보") @RequestBody @Valid VersionCreateRequest request);

  @GetMapping
  @Operation(
      summary = "[관리자] 버전 목록 조회",
      description = "고정된 데이터셋 버전을 최신순으로 조회합니다. 포함 이미지 수, 배치명, 고정 일시·작성자를 함께 반환합니다.")
  ResponseEntity<BaseResponse<List<VersionResponse>>> getVersions();

  @GetMapping("/{versionId}")
  @Operation(
      summary = "[관리자] 버전 상세 조회",
      description =
          "버전 통계(이미지 수, annotation 수, 라벨된 키포인트 수)와 포함 이미지 상세 목록을 반환합니다. "
              + "고정된 COCO 에서 값을 읽으므로 화면 수치와 실제 내보내기 결과가 항상 일치합니다.")
  ResponseEntity<BaseResponse<VersionDetailResponse>> getVersion(
      @Parameter(description = "버전 식별자") @PathVariable Long versionId);

  @DeleteMapping("/{versionId}")
  @Operation(
      summary = "[관리자] 버전 삭제",
      description =
          "고정된 버전 레코드만 삭제합니다. (Hard Delete) 원본 라벨과 승인 스냅샷은 건드리지 않으므로, "
              + "같은 승인본으로 버전을 다시 만들 수 있습니다.")
  ResponseEntity<BaseResponse<Void>> deleteVersion(
      @Parameter(description = "버전 식별자") @PathVariable Long versionId);

  @GetMapping("/{versionId}/export")
  @Operation(
      summary = "[관리자] COCO JSON 내보내기",
      description =
          "COCO keypoints 포맷 JSON 전문을 반환합니다. 공통 응답 래퍼(BaseResponse) 없이 COCO 규격 그대로 내려주므로 "
              + "학습 프레임워크에 변환 없이 바로 로드할 수 있습니다. 미리보기와 다운로드가 같은 내용을 봅니다.")
  @ApiResponse(
      responseCode = "200",
      description = "COCO keypoints JSON",
      content = @Content(mediaType = "application/json"))
  ResponseEntity<String> exportCoco(
      @Parameter(description = "버전 식별자") @PathVariable Long versionId);
}
