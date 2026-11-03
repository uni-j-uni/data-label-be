/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.signtelecom.datalabelbe.domain.review.dto.response.CommentResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "검수", description = "제출된 라벨의 검수·코멘트 API")
@RequestMapping("/api/images/{imageId}/comments")
public interface ImageCommentController {

  @GetMapping
  @Operation(
      summary = "이미지 검수 코멘트 조회",
      description =
          "이미지에 달린 검수 코멘트를 최신순으로 조회합니다. 제출 건을 넘나들며 모으므로 여러 번 반려된 이미지의 지난 사유까지 한 번에 볼 수 있고, "
              + "각 코멘트의 submissionId 로 어느 제출 건의 지적인지 구분할 수 있습니다. "
              + "targetObjectIndex 는 인물의 표시 순서(0부터)이며 null 이면 이미지 전체에 대한 코멘트입니다. "
              + "배치 소유자·관리자·담당 작업자가 조회할 수 있습니다.")
  ResponseEntity<BaseResponse<List<CommentResponse>>> getImageComments(
      @Parameter(description = "이미지 식별자") @PathVariable Long imageId);
}
