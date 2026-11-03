/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.service;

import com.signtelecom.datalabelbe.domain.label.dto.request.PoseLabelSaveRequest;
import com.signtelecom.datalabelbe.domain.label.dto.response.PoseLabelResponse;
import com.signtelecom.datalabelbe.global.exception.CustomException;

/** 이미지 한 장의 포즈 라벨을 조회·저장하는 비즈니스 로직을 처리하는 서비스 */
public interface LabelService {

  /**
   * 이미지의 라벨을 조회
   *
   * <p>아직 저장된 적이 없으면 빈 객체 목록으로 응답
   *
   * <p>배치 소유자·관리자·담당 작업자가 조회할 수 있음. 검수에서도 같은 응답을 읽기 전용으로 사용
   *
   * @param imageId 조회할 이미지 식별자
   * @return 라벨 전체와 작업 상태, 수정 가능 여부
   * @throws CustomException 이미지가 없거나 접근 권한이 없는 경우
   */
  PoseLabelResponse getLabel(Long imageId);

  /**
   * 이미지의 라벨을 임시 저장
   *
   * <p>요청으로 받은 객체 목록으로 기존 객체를 모두 교체. 담당 작업자만 저장할 수 있고, 검수 대기·승인 완료 상태에서는 거부
   *
   * <p>클라이언트 검증을 신뢰하지 않고 서버에서 다시 확인. 객체마다 키포인트가 정확히 17종인지, 중복이 없는지, 좌표와 바운딩 박스가 이미지 범위 안에 있는지 봄.
   * COCO export 시점에 포맷이 깨지면 원인을 추적하기 어려우므로 저장 시점에 막음
   *
   * @param imageId 저장할 이미지 식별자
   * @param request 인물 객체 목록
   * @return 저장된 라벨 전체
   * @throws CustomException 담당 작업자가 아니거나 수정 불가 상태인 경우, 검증에 실패한 경우
   */
  PoseLabelResponse saveLabel(Long imageId, PoseLabelSaveRequest request);
}
