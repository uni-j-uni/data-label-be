/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.signtelecom.datalabelbe.domain.review.dto.request.ReviewRequest;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionResponse;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionSummaryResponse;
import com.signtelecom.datalabelbe.domain.review.entity.SubmissionStatus;
import com.signtelecom.datalabelbe.global.page.response.PageResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[관리자] 검수", description = "검수 목록 조회와 승인·반려 관리자 API")
@RequestMapping("/api/admin/reviews")
public interface AdminReviewController {

  @GetMapping
  @Operation(
      summary = "[관리자] 검수 목록 조회 (페이지)",
      description =
          "제출 건 목록을 제출 시각 최신순으로 페이지 단위 조회합니다. status 는 여러 개 줄 수 있어 "
              + "'검수 대기' 탭은 status=PENDING_REVIEW, '검수 이력' 탭은 status=APPROVED&status=REJECTED 로 조회합니다. "
              + "page 는 1부터 시작하며 size 는 1~100 입니다(범위를 벗어나면 400 PAGE4002, page 가 1 미만이면 404 PAGE4001). "
              + "범위를 넘는 page 는 빈 content 로 응답합니다. 탭 배지 숫자는 GET /api/admin/reviews/summary 로 받으세요.")
  ResponseEntity<BaseResponse<PageResponse<SubmissionResponse>>> getSubmissions(
      @Parameter(description = "검수 상태 필터. 여러 개 가능, 생략하면 전체") @RequestParam(required = false)
          List<SubmissionStatus> status,
      @Parameter(description = "배치 필터. 생략하면 전체 배치") @RequestParam(required = false) Long batchId,
      @Parameter(description = "페이지 번호 (1부터)", example = "1") @RequestParam(defaultValue = "1")
          int page,
      @Parameter(description = "페이지 크기 (1~100)", example = "20") @RequestParam(defaultValue = "20")
          int size);

  @GetMapping("/summary")
  @Operation(
      summary = "[관리자] 검수 상태별 건수",
      description = "상태별 제출 건수를 조회합니다. 목록이 페이지 단위이므로 탭 배지 숫자는 이 응답으로 채웁니다. 건이 없는 상태도 0으로 포함됩니다.")
  ResponseEntity<BaseResponse<SubmissionSummaryResponse>> getSubmissionSummary(
      @Parameter(description = "배치 필터. 생략하면 전체 배치") @RequestParam(required = false) Long batchId);

  @PostMapping
  @Operation(
      summary = "[관리자] 승인 · 반려",
      description =
          "제출 건을 승인하거나 반려합니다. 승인 시 그 시점의 라벨을 값으로 복사한 스냅샷을 만들어 고정하므로, 이후 원본이 수정돼도 승인본은 변하지 않습니다. "
              + "반려 시 작업 할당이 REJECTED로 바뀌며, 담당 작업자가 이어서 수정하고 재제출할 수 있습니다. 재작업 근거가 필요하므로 "
              + "코멘트를 최소 1건 요구합니다(400 REVIEW4003).")
  ResponseEntity<BaseResponse<SubmissionResponse>> review(
      @Parameter(description = "검수 판정과 코멘트") @RequestBody @Valid ReviewRequest request);
}
