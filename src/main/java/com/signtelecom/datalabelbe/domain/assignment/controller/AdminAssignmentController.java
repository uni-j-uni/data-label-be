/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.signtelecom.datalabelbe.domain.assignment.dto.request.AssignmentCreateRequest;
import com.signtelecom.datalabelbe.domain.assignment.dto.request.AssignmentReleaseRequest;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentCreateResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentReleaseResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[관리자] 작업 할당", description = "이미지를 작업자에게 배정·해제하고 현황을 조회하는 관리자 API")
@RequestMapping("/api/admin/assignments")
public interface AdminAssignmentController {

  @PostMapping
  @Operation(
      summary = "[관리자] 작업 할당",
      description =
          "이미지들을 한 작업자(ROLE_USER)에게 할당합니다. 새 할당은 IN_PROGRESS로 시작합니다. "
              + "이미 할당된 이미지는 담당 작업자만 교체하고 진행 상태를 유지하므로 기존 라벨 작업을 이어서 할 수 있습니다. "
              + "승인 완료(APPROVED)된 건은 건너뛰며, 건너뛴 이미지는 응답의 skippedImageIds로 알려줍니다.")
  ResponseEntity<BaseResponse<AssignmentCreateResponse>> createAssignments(
      @Parameter(description = "할당 정보") @RequestBody @Valid AssignmentCreateRequest request);

  @DeleteMapping
  @Operation(
      summary = "[관리자] 작업 할당 해제",
      description =
          "아직 손대지 않은 할당(IN_PROGRESS이면서 라벨을 한 번도 저장하지 않은 건)만 해제해 미할당(대기) 상태로 되돌립니다. "
              + "라벨이 저장된 건(객체 0개로 저장한 경우 포함), 반려·검수대기·승인 건은 건너뛰고 skippedImageIds로 알려줍니다. "
              + "할당이 없던 이미지는 released·skipped 어디에도 포함하지 않습니다. 담당자 변경은 작업 할당(POST) API로 재할당하세요.")
  ResponseEntity<BaseResponse<AssignmentReleaseResponse>> releaseAssignments(
      @Parameter(description = "해제할 이미지 목록") @RequestBody @Valid AssignmentReleaseRequest request);

  @GetMapping
  @Operation(
      summary = "[관리자] 배치 할당 현황 조회",
      description = "배치에 속한 이미지들의 담당 작업자와 상태를 조회합니다. 화면에서 작업자별 요약을 집계하는 데 사용합니다.")
  ResponseEntity<BaseResponse<List<AssignmentResponse>>> getAssignmentsByBatch(
      @Parameter(description = "배치 식별자") @RequestParam Long batchId);
}
