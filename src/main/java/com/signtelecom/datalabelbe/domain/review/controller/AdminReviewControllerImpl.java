/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.review.dto.request.ReviewRequest;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionResponse;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionSummaryResponse;
import com.signtelecom.datalabelbe.domain.review.entity.SubmissionStatus;
import com.signtelecom.datalabelbe.domain.review.service.ReviewService;
import com.signtelecom.datalabelbe.global.page.response.PageResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminReviewControllerImpl implements AdminReviewController {

  private final ReviewService reviewService;

  @Override
  public ResponseEntity<BaseResponse<PageResponse<SubmissionResponse>>> getSubmissions(
      List<SubmissionStatus> status, Long batchId, int page, int size) {

    return ResponseEntity.ok(
        BaseResponse.success(reviewService.getSubmissions(status, batchId, page, size)));
  }

  @Override
  public ResponseEntity<BaseResponse<SubmissionSummaryResponse>> getSubmissionSummary(
      Long batchId) {

    return ResponseEntity.ok(BaseResponse.success(reviewService.getSubmissionSummary(batchId)));
  }

  @Override
  public ResponseEntity<BaseResponse<SubmissionResponse>> review(ReviewRequest request) {

    return ResponseEntity.ok(
        BaseResponse.success(200, "검수 처리가 완료되었습니다.", reviewService.review(request)));
  }
}
