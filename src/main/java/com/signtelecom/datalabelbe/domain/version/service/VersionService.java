/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.service;

import java.util.List;

import com.signtelecom.datalabelbe.domain.version.dto.request.VersionCreateRequest;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionDetailResponse;
import com.signtelecom.datalabelbe.domain.version.dto.response.VersionResponse;
import com.signtelecom.datalabelbe.global.exception.CustomException;

/** 승인된 라벨을 데이터셋 버전으로 고정하고 COCO 로 내보내는 비즈니스 로직을 처리하는 서비스 */
public interface VersionService {

  /**
   * 배치에서 현재 승인된 라벨로 데이터셋 버전을 만듦
   *
   * <p>승인 스냅샷들로 COCO JSON 을 만들어 그대로 저장. 이후 승인 건이 늘어나도 이미 만든 버전의 내용은 바뀌지 않음
   *
   * @param request 배치 식별자와 버전 이름
   * @return 생성된 버전 요약
   * @throws CustomException 배치가 없거나 접근 권한이 없는 경우, 승인된 라벨이 하나도 없는 경우
   */
  VersionResponse createVersion(VersionCreateRequest request);

  /**
   * 버전 목록을 최신순으로 조회
   *
   * @return 버전 요약 목록
   */
  List<VersionResponse> getVersions();

  /**
   * 버전의 통계와 포함 이미지 상세를 조회
   *
   * <p>고정된 COCO 에서 값을 읽으므로 내보내기 결과와 항상 일치
   *
   * @param versionId 조회할 버전 식별자
   * @return 버전 통계와 이미지별 상세
   * @throws CustomException 버전이 없는 경우
   */
  VersionDetailResponse getVersion(Long versionId);

  /**
   * 버전의 COCO JSON 전문을 반환
   *
   * <p>고정 시점에 만들어 둔 문자열을 그대로 돌려줌. 미리보기와 다운로드가 같은 내용을 보게 됨
   *
   * @param versionId 내보낼 버전 식별자
   * @return COCO keypoints 포맷 JSON
   * @throws CustomException 버전이 없는 경우
   */
  String exportCoco(Long versionId);

  /**
   * 데이터셋 버전을 삭제 (관리자 전용)
   *
   * <p>버전 레코드만 삭제. 원본 라벨과 승인 스냅샷은 남아 있어 같은 내용으로 다시 생성 가능
   *
   * @param versionId 삭제할 버전 식별자
   * @throws CustomException 버전이 없는 경우
   */
  void deleteVersion(Long versionId);

  /**
   * 다운로드 파일명을 만듦
   *
   * @param versionId 대상 버전 식별자
   * @return {배치명}_{버전명}.json 형태의 파일명
   * @throws CustomException 버전이 없는 경우
   */
  String exportFilename(Long versionId);
}
