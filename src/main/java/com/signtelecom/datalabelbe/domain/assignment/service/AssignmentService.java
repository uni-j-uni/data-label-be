/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.service;

import java.util.List;

import com.signtelecom.datalabelbe.domain.assignment.dto.request.AssignmentCreateRequest;
import com.signtelecom.datalabelbe.domain.assignment.dto.request.AssignmentReleaseRequest;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentCreateResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentReleaseResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentResponse;
import com.signtelecom.datalabelbe.domain.assignment.dto.response.MyAssignmentSummaryResponse;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;
import com.signtelecom.datalabelbe.global.exception.CustomException;
import com.signtelecom.datalabelbe.global.page.response.InfiniteResponse;

/** 이미지를 작업자에게 배정하고, 배정 현황을 조회하는 비즈니스 로직을 처리하는 서비스 */
public interface AssignmentService {

  /**
   * 이미지들을 한 작업자에게 할당 (관리자 전용)
   *
   * <p>새 할당은 IN_PROGRESS로 시작. 이미 할당된 이미지는 담당 작업자만 교체하고 진행 상태를 유지하므로, 라벨 작업을 이어서 할 수 있음
   *
   * <p>단 승인 완료(APPROVED)된 건은 재할당하지 않고 건너뜀. 승인된 건의 담당자가 바뀌면 검수 이력과 맞지 않기 때문. 건너뛴 건은 응답의
   * skippedImageIds로 알려줌
   *
   * @param request 작업자 식별자와 이미지 식별자 목록
   * @return 실제 할당 건수, 건너뛴 건수, 할당 정보 목록
   * @throws CustomException 작업자나 이미지가 없는 경우, 작업자가 ROLE_USER가 아닌 경우
   */
  AssignmentCreateResponse createAssignments(AssignmentCreateRequest request);

  /**
   * 이미지들의 작업 할당을 해제 (관리자 전용)
   *
   * <p>아직 손대지 않은 할당(IN_PROGRESS + 라벨 미저장)만 삭제해 미할당(대기) 상태로 되돌림. 라벨이 저장됐거나(객체 0개 포함) 반려·검수대기·승인 상태인
   * 건은 건너뛰고 skipped 로 반환하며 RELEASE 이력도 남기지 않음. 할당이 없던 이미지는 어느 쪽에도 포함하지 않음
   *
   * <p>담당자 변경은 해제 대신 재할당(createAssignments)으로 처리
   *
   * @param request 해제할 이미지 식별자 목록
   * @return 해제·건너뜀 건수와 이미지 식별자 목록
   * @throws CustomException 이미지 식별자 목록이 비어 있는 경우
   */
  AssignmentReleaseResponse releaseAssignments(AssignmentReleaseRequest request);

  /**
   * 배치에 속한 이미지들의 할당 현황을 조회 (관리자 전용)
   *
   * <p>이미지별 담당 작업자와 상태를 함께 반환
   *
   * @param batchId 조회할 배치 식별자
   * @return 할당 정보 목록
   * @throws CustomException 배치가 없거나 접근 권한이 없는 경우
   */
  List<AssignmentResponse> getAssignmentsByBatch(Long batchId);

  /**
   * 작업을 검수에 제출 (담당 작업자 전용)
   *
   * <p>상태를 PENDING_REVIEW(검수대기)로 바꿔 검수자의 목록에 올림. 임시 저장과 제출을 분리한 이유는, 저장은 수시로 하되 아직 검수에 넘길 준비가 안 된
   * 경우가 있어서. 하나의 API로 묶으면 미완성 라벨이 실수로 제출되는 것을 막기 어려움
   *
   * <p>인물 객체가 하나도 없으면 제출을 거부. 빈 라벨이 검수 대기열에 쌓이는 것을 막기 위함
   *
   * @param assignmentId 제출할 할당 식별자
   * @return 제출 후의 할당 정보
   * @throws CustomException 할당이 없거나 담당 작업자가 아닌 경우, 이미 제출·승인된 경우, 객체가 없는 경우
   */
  AssignmentResponse submit(Long assignmentId);

  /**
   * 현재 인증된 작업자에게 할당된 작업 목록을 커서 단위로 조회 (최신순)
   *
   * <p>클라이언트가 파라미터를 조작해 다른 작업자의 할당을 보는 것을 막기 위해, 대상 작업자를 파라미터로 받지 않고 서버가 인증 정보에서 강제로 결정
   *
   * @param statuses 필터할 상태 목록. null 이거나 비어 있으면 전체
   * @param cursor 이전 응답의 lastCursor. null 이면 처음부터
   * @param size 가져올 개수 (1~100)
   * @return 본인에게 할당된 작업 묶음
   * @throws CustomException 인증 정보가 없거나 크기가 범위를 벗어난 경우
   */
  InfiniteResponse<AssignmentResponse> getMyAssignments(
      List<AssignmentStatus> statuses, Long cursor, int size);

  /**
   * 현재 인증된 작업자의 상태별 할당 수와 이어서 작업할 이미지를 조회
   *
   * @return 상태별 건수와 이어서 작업할 이미지 식별자
   */
  MyAssignmentSummaryResponse getMyAssignmentSummary();

  /**
   * 현재 인증된 작업자에게 할당된 이미지 중 한 배치의 이미지 식별자만 최신순으로 조회
   *
   * <p>목록이 커서 단위라 에디터가 이전/다음 이미지를 찾을 수 있도록 식별자만 따로 제공
   *
   * @param batchId 배치 식별자
   * @return 이미지 식별자 목록
   */
  List<Long> getMyImageIds(Long batchId);
}
