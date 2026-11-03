/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.review.dto.response.CommentResponse;
import com.signtelecom.datalabelbe.domain.review.service.ReviewService;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ImageCommentControllerImpl implements ImageCommentController {

  private final ReviewService reviewService;

  @Override
  public ResponseEntity<BaseResponse<List<CommentResponse>>> getImageComments(Long imageId) {

    return ResponseEntity.ok(BaseResponse.success(reviewService.getImageComments(imageId)));
  }
}
