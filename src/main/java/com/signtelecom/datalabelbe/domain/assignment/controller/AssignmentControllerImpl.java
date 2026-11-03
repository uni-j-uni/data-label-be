/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.MyAssignmentSummaryResponse;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.domain.assignment.service.AssignmentService;
import com.signtelecom.datalabelbe.global.page.response.InfiniteResponse;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AssignmentControllerImpl implements AssignmentController {

  private final AssignmentService assignmentService;

  @Override
  public ResponseEntity<BaseResponse<AssignmentResponse>> submit(Long assignmentId) {

    return ResponseEntity.ok(
        BaseResponse.success(200, "검수 요청이 완료되었습니다.", assignmentService.submit(assignmentId)));
  }

  @Override
  public ResponseEntity<BaseResponse<InfiniteResponse<AssignmentResponse>>> getMyAssignments(
      List<AssignmentStatus> status, Long cursor, int size) {

    return ResponseEntity.ok(
        BaseResponse.success(assignmentService.getMyAssignments(status, cursor, size)));
  }

  @Override
  public ResponseEntity<BaseResponse<MyAssignmentSummaryResponse>> getMyAssignmentSummary() {

    return ResponseEntity.ok(BaseResponse.success(assignmentService.getMyAssignmentSummary()));
  }

  @Override
  public ResponseEntity<BaseResponse<List<Long>>> getMyImageIds(Long batchId) {

    return ResponseEntity.ok(BaseResponse.success(assignmentService.getMyImageIds(batchId)));
  }
}
