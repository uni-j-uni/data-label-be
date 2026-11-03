/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.signtelecom.datalabelbe.domain.assignment.dto.request.AssignmentCreateRequest;
import com.signtelecom.datalabelbe.domain.assignment.dto.request.AssignmentReleaseRequest;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentCreateResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentReleaseResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentResponse;
import com.signtelecom.datalabelbe.domain.assignment.service.AssignmentService;
import com.signtelecom.datalabelbe.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminAssignmentControllerImpl implements AdminAssignmentController {

  private final AssignmentService assignmentService;

  @Override
  public ResponseEntity<BaseResponse<AssignmentCreateResponse>> createAssignments(
      AssignmentCreateRequest request) {

    AssignmentCreateResponse response = assignmentService.createAssignments(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success(201, "작업 할당이 완료되었습니다.", response));
  }

  @Override
  public ResponseEntity<BaseResponse<AssignmentReleaseResponse>> releaseAssignments(
      AssignmentReleaseRequest request) {

    return ResponseEntity.ok(BaseResponse.success(assignmentService.releaseAssignments(request)));
  }

  @Override
  public ResponseEntity<BaseResponse<List<AssignmentResponse>>> getAssignmentsByBatch(
      Long batchId) {

    return ResponseEntity.ok(
        BaseResponse.success(assignmentService.getAssignmentsByBatch(batchId)));
  }
}
