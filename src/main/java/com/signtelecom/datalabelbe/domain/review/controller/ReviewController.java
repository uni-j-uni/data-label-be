/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionDetailResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "검수", description = "검수 상세 조회 API (관리자·제출자 본인)")
@RequestMapping("/api/reviews")
public interface ReviewController {

  @GetMapping("/{submissionId}")
  @Operation(
      summary = "검수 상세 조회",
      description =
          "제출 건의 라벨과 코멘트를 조회합니다. 검수 대기 건은 현재 라벨(labelSource=LIVE), 승인 건은 승인 시점 고정본(labelSource=SNAPSHOT)을 "
              + "돌려줍니다. 두 경우 모두 동일한 라벨 응답 형태이므로 라벨링 캔버스를 읽기 전용으로 그대로 재사용할 수 있습니다. "
              + "관리자 외에 제출자 본인도 조회할 수 있어, 반려 사유를 읽고 재작업할 수 있습니다.")
  ResponseEntity<BaseResponse<SubmissionDetailResponse>> getSubmission(
      @Parameter(description = "제출 식별자") @PathVariable Long submissionId);
}
