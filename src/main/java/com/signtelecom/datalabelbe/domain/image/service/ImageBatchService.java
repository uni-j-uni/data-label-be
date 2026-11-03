/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.service;

import java.util.List;

import com.signtelecom.datalabelbe.domain.image.dto.request.ImageBatchRequest;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageBatchResponse;
import com.signtelecom.datalabelbe.domain.image.entity.ImageBatch;
import com.signtelecom.datalabelbe.global.exception.CustomException;

/** 이미지 배치 생성, 조회, 수정, 삭제 등의 비즈니스 로직을 처리하는 서비스 */
public interface ImageBatchService {

  /**
   * 현재 인증된 사용자를 소유자로 하는 이미지 배치를 생성하고 엔티티를 반환
   *
   * <p>이름이 비어 있으면 현재 날짜와 시·분으로 자동 생성. 업로드와 같은 트랜잭션에서 배치를 만들어 쓰기 위한 메서드
   *
   * @param name 배치 이름. null이거나 공백이면 현재 날짜와 시·분으로 자동 생성
   * @return 생성된 배치 엔티티
   * @throws CustomException 인증 정보가 없는 경우
   */
  ImageBatch createBatch(String name);

  /**
   * 전체 이미지 배치 목록을 최신순으로 조회
   *
   * <p>배치별 이미지 수는 단일 집계 쿼리로 계산. ADMIN 전용 조회이므로 소유자와 무관하게 모든 배치를 반환
   *
   * @return 배치 정보 목록
   */
  List<ImageBatchResponse> getBatches();

  /**
   * 배치 하나의 상세 정보를 조회
   *
   * @param batchId 조회할 배치 식별자
   * @return 배치 정보를 담은 응답 DTO
   * @throws CustomException 배치가 없거나 접근 권한이 없는 경우
   */
  ImageBatchResponse getBatch(Long batchId);

  /**
   * 배치의 이름을 변경
   *
   * @param batchId 수정할 배치 식별자
   * @param request 변경할 이름을 담은 요청 DTO
   * @return 수정된 배치 정보를 담은 응답 DTO
   * @throws CustomException 배치가 없거나 현재 사용자의 배치가 아닌 경우
   */
  ImageBatchResponse updateBatch(Long batchId, ImageBatchRequest request);

  /**
   * 배치와 그에 속한 모든 이미지를 삭제
   *
   * <p>DB 레코드와 함께 저장소에 기록된 실제 파일도 정리
   *
   * @param batchId 삭제할 배치 식별자
   * @throws CustomException 배치가 없거나 현재 사용자의 배치가 아닌 경우
   */
  void deleteBatch(Long batchId);

  /**
   * 배치를 조회하고 현재 인증된 사용자가 접근할 수 있는지 검증
   *
   * <p>배치 소유자이거나 ADMIN이면 통과. 이미지 도메인 내부에서 접근 검증을 재사용하기 위한 메서드
   *
   * @param batchId 조회할 배치 식별자
   * @return 접근 권한이 검증된 배치 엔티티
   * @throws CustomException 배치가 없거나 접근 권한이 없는 경우
   */
  ImageBatch getOwnedBatch(Long batchId);
}
