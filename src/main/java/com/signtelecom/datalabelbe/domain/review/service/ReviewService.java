/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.service;

import java.util.List;

import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.review.dto.request.ReviewRequest;
import com.signtelecom.datalabelbe.domain.review.dto.response.CommentResponse;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionDetailResponse;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionResponse;
import com.signtelecom.datalabelbe.domain.review.dto.response.SubmissionSummaryResponse;
import com.signtelecom.datalabelbe.domain.review.entity.SubmissionStatus;
import com.signtelecom.datalabelbe.domain.user.entity.User;
import com.signtelecom.datalabelbe.global.exception.CustomException;
import com.signtelecom.datalabelbe.global.page.response.PageResponse;

/** 제출된 라벨을 검수하고 승인 시점 데이터를 고정하는 비즈니스 로직을 처리하는 서비스 */
public interface ReviewService {

  /**
   * 검수 목록을 페이지 단위로 조회 (관리자 전용)
   *
   * <p>제출 시각 최신순. 상태를 여러 개 주면 그중 하나에 해당하는 건을, 생략하면 전체를 반환
   *
   * @param statuses 필터할 검수 상태 목록. null 이거나 비어 있으면 전체
   * @param batchId 필터할 배치. null 이면 전체 배치
   * @param page 1부터 시작하는 페이지 번호
   * @param size 페이지 크기 (1~100)
   * @return 제출 건 요약 페이지
   * @throws CustomException 페이지 번호가 1 미만이거나 크기가 범위를 벗어난 경우
   */
  PageResponse<SubmissionResponse> getSubmissions(
      List<SubmissionStatus> statuses, Long batchId, int page, int size);

  /**
   * 검수 상태별 제출 건수를 조회 (관리자 전용)
   *
   * <p>목록이 페이지 단위라 탭 배지 숫자처럼 전체 건수가 필요한 곳에서 사용
   *
   * @param batchId 필터할 배치. null 이면 전체 배치
   * @return 상태별 건수
   */
  SubmissionSummaryResponse getSubmissionSummary(Long batchId);

  /**
   * 제출 건 하나의 상세를 조회
   *
   * <p>검수 대기 건은 현재 라벨을, 승인 건은 승인 시점에 고정된 스냅샷을 돌려줌. 어느 쪽이든 동일한 라벨 응답 형태
   *
   * <p>관리자 외에 제출자 본인도 조회할 수 있음. 반려 사유 코멘트를 읽고 재작업해야 하기 때문
   *
   * @param submissionId 조회할 제출 식별자
   * @return 제출 요약, 라벨, 코멘트
   * @throws CustomException 제출 건이 없거나 접근 권한이 없는 경우
   */
  SubmissionDetailResponse getSubmission(Long submissionId);

  /**
   * 제출 건을 승인하거나 반려 (관리자 전용)
   *
   * <p>승인 시 라벨을 복사해 스냅샷을 만듦. 원본 라벨은 이후에도 수정될 수 있으므로 승인 당시 내용을 따로 보관
   *
   * <p>반려 시 작업 할당이 REJECTED로 바뀌고, 이 상태에서도 라벨 저장과 재제출 가능. 반려에는 코멘트 최소 1건 필요
   *
   * @param request 제출 식별자, 판정, 코멘트 목록
   * @return 검수 후의 제출 요약
   * @throws CustomException 제출 건이 없거나 이미 검수된 경우, 반려인데 코멘트가 없는 경우
   */
  SubmissionResponse review(ReviewRequest request);

  /**
   * 이미지에 달린 검수 코멘트를 최신순으로 조회
   *
   * <p>모든 제출 건의 코멘트를 함께 조회하므로 이전 반려 사유까지 확인 가능. 각 코멘트의 submissionId 로 어느 제출 건인지 구분
   *
   * <p>배치 소유자·관리자·담당 작업자가 조회할 수 있음
   *
   * @param imageId 대상 이미지 식별자
   * @return 코멘트 목록 (최신순)
   * @throws CustomException 이미지가 없거나 접근 권한이 없는 경우
   */
  List<CommentResponse> getImageComments(Long imageId);

  /**
   * 검수 요청 시 제출 이력을 생성
   *
   * <p>작업 할당 도메인에서 제출을 처리할 때 호출. 반려 후 재제출하면 새 이력이 쌓여 과거 제출 건을 그대로 조회할 수 있음
   *
   * @param image 제출 대상 이미지
   * @param submittedBy 제출한 작업자
   * @param objectCount 제출 당시 인물 객체 수
   * @param labeledKeypointCount 제출 당시 찍힌 키포인트 수
   */
  void createSubmission(Image image, User submittedBy, int objectCount, int labeledKeypointCount);
}
