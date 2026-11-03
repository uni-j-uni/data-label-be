/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.signtelecom.datalabelbe.domain.image.dto.response.ImageResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "이미지", description = "이미지 상세·파일 조회 API (관리자·담당 작업자)")
@RequestMapping("/api/image-batches/{batchId}/images")
public interface ImageController {

  @GetMapping("/{imageId}")
  @Operation(
      summary = "이미지 상세 조회",
      description =
          "이미지 한 건을 작업 할당 현황과 함께 조회합니다. 파일명·배치명·해상도·작업 상태를 한 번에 내려주므로 라벨링 에디터 헤더에 바로 쓸 수 있습니다. "
              + "배치 소유자와 관리자 외에 그 이미지를 할당받은 작업자도 조회할 수 있습니다.")
  ResponseEntity<BaseResponse<ImageResponse>> getImage(
      @Parameter(description = "배치 식별자") @PathVariable Long batchId,
      @Parameter(description = "이미지 식별자") @PathVariable Long imageId);

  @GetMapping("/{imageId}/file")
  @Operation(
      summary = "이미지 파일 조회",
      description =
          "이미지 바이너리를 반환합니다. 브라우저가 <img> 태그로 직접 참조할 수 있도록 공통 응답 래퍼(BaseResponse) 없이 "
              + "파일 바이트를 그대로 내려줍니다. DB 레코드는 있으나 저장소에 파일이 없으면 404(IMAGE4009)를 반환하므로, "
              + "클라이언트는 '로딩 중'과 '미리보기 없음'을 구분할 수 있습니다. "
              + "w 를 주면 비율을 유지한 채 그 너비로 줄인 JPEG 썸네일을 반환하며, 생성 결과는 캐시됩니다. 원본보다 큰 너비는 확대하지 않습니다.")
  @ApiResponse(
      responseCode = "200",
      description = "이미지 바이너리",
      content = {
        @Content(mediaType = MediaType.IMAGE_JPEG_VALUE),
        @Content(mediaType = MediaType.IMAGE_PNG_VALUE)
      })
  ResponseEntity<Resource> getImageFile(
      @Parameter(description = "배치 식별자") @PathVariable Long batchId,
      @Parameter(description = "이미지 식별자") @PathVariable Long imageId,
      @Parameter(description = "썸네일 너비(px, 16~2048). 생략하면 원본을 반환")
          @RequestParam(value = "w", required = false)
          Integer w);
}
