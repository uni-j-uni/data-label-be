/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.MyAssignmentSummaryResponse;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.global.page.response.InfiniteResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "작업 할당", description = "작업자 본인의 할당 조회·제출 API")
@RequestMapping("/api/assignments")
public interface AssignmentController {

  @PostMapping("/{assignmentId}/submit")
  @Operation(
      summary = "검수 요청 (제출)",
      description =
          "담당 작업자가 라벨링을 마치고 검수를 요청합니다. 상태가 PENDING_REVIEW(검수대기)로 바뀝니다. "
              + "임시 저장과 분리되어 있어 미완성 라벨이 실수로 제출되지 않습니다. "
              + "인물 객체가 하나도 없으면 400(LABEL4008)으로 거부합니다.")
  ResponseEntity<BaseResponse<AssignmentResponse>> submit(
      @Parameter(description = "할당 식별자") @PathVariable Long assignmentId);

  @GetMapping("/me")
  @Operation(
      summary = "내 작업 목록 조회 (무한 스크롤)",
      description =
          "현재 로그인한 작업자에게 할당된 작업을 최신순으로 커서 단위 조회합니다. 첫 요청은 cursor 없이 보내고, 다음 묶음은 "
              + "응답의 lastCursor 를 cursor 로 넘깁니다. hasNext 가 false 이면 끝입니다. status 는 여러 개 줄 수 있습니다. "
              + "size 는 1~100 입니다(범위를 벗어나면 400 PAGE4002). 대상 작업자는 인증 정보로 서버가 강제 결정하므로 다른 작업자의 할당을 "
              + "조회할 수 없습니다. 상단 건수와 '이어서 작업하기'는 GET /api/assignments/me/summary 로 받으세요.")
  ResponseEntity<BaseResponse<InfiniteResponse<AssignmentResponse>>> getMyAssignments(
      @Parameter(description = "상태 필터. 여러 개 가능, 생략하면 전체") @RequestParam(required = false)
          List<AssignmentStatus> status,
      @Parameter(description = "이전 응답의 lastCursor. 첫 요청은 생략") @RequestParam(required = false)
          Long cursor,
      @Parameter(description = "가져올 개수 (1~100)", example = "60") @RequestParam(defaultValue = "60")
          int size);

  @GetMapping("/me/summary")
  @Operation(
      summary = "내 작업 현황",
      description =
          "상태별 할당 수와 이어서 작업할 이미지(nextImageId)를 조회합니다. nextImageId 는 수정 가능(진행중·반려)한 할당 중 가장 최근 것이며 "
              + "없으면 null 입니다. 건이 없는 상태도 0으로 포함됩니다.")
  ResponseEntity<BaseResponse<MyAssignmentSummaryResponse>> getMyAssignmentSummary();

  @GetMapping("/me/image-ids")
  @Operation(
      summary = "내 작업 이미지 식별자 (배치별)",
      description = "본인에게 할당된 이미지 중 해당 배치의 이미지 식별자만 최신순으로 조회합니다. 에디터의 이전/다음 이동에 사용합니다.")
  ResponseEntity<BaseResponse<List<Long>>> getMyImageIds(
      @Parameter(description = "배치 식별자", required = true) @RequestParam Long batchId);
}
