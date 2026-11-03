/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionDetailResponse;
import com.signtelecom.datalabelbe.domain.review.service.ReviewService;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReviewControllerImpl implements ReviewController {

  private final ReviewService reviewService;

  @Override
  public ResponseEntity<BaseResponse<SubmissionDetailResponse>> getSubmission(Long submissionId) {

    return ResponseEntity.ok(BaseResponse.success(reviewService.getSubmission(submissionId)));
  }
}
