/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.signtelecom.datalabelbe.domain.image.dto.response.ImageResponse;
import com.signtelecom.datalabelbe.domain.image.dto.response.ImageUploadResponse;
import com.signtelecom.datalabelbe.global.exception.CustomException;

/** 배치에 속한 이미지의 업로드, 조회, 삭제 비즈니스 로직을 처리하는 서비스 */
public interface ImageService {

  /**
   * 배치를 새로 만들고 이미지 파일들을 업로드
   *
   * <p>배치 이름이 비어 있으면 현재 날짜와 시·분으로 자동 생성
   *
   * @param name 배치 이름. null이거나 공백이면 현재 날짜와 시·분으로 자동 생성
   * @param files 업로드할 이미지 파일 목록
   * @return 생성된 배치 식별자와 파일별 처리 결과
   * @throws CustomException 인증 정보가 없거나 파일이 하나도 전달되지 않은 경우
   */
  ImageUploadResponse createBatchAndUpload(String name, List<MultipartFile> files);

  /**
   * 이미 존재하는 배치에 이미지 파일들을 추가로 업로드
   *
   * <p>용량 제한과 포맷(JPEG, PNG)은 클라이언트 검증을 신뢰하지 않고 서버에서 다시 확인. 저장 파일명은 UUID로 생성해 충돌을 방지하고,
   * width/height는 이미지를 읽으면서 함께 채움
   *
   * <p>검증에 실패한 파일은 통과한 파일의 저장을 막지 않음. 실패 사유는 응답의 failures에 모아 반환
   *
   * @param batchId 이미지를 추가할 배치 식별자
   * @param files 업로드할 이미지 파일 목록
   * @return 배치 식별자와 파일별 처리 결과
   * @throws CustomException 배치가 없거나 권한이 없는 경우, 파일이 하나도 전달되지 않은 경우
   */
  ImageUploadResponse uploadImages(Long batchId, List<MultipartFile> files);

  /**
   * 배치에 속한 이미지 목록을 할당 현황과 함께 조회
   *
   * <p>할당이 없는 이미지는 status를 PENDING(대기)으로 채워 함께 반환. 할당은 이미지마다 조회하지 않고 배치 단위로 한 번에 가져와 병합
   *
   * @param batchId 조회할 배치 식별자
   * @return 이미지 정보와 할당 현황을 합친 목록
   * @throws CustomException 배치가 없거나 접근 권한이 없는 경우
   */
  List<ImageResponse> getImages(Long batchId);

  /**
   * 이미지 한 건을 할당 현황과 함께 조회
   *
   * <p>파일명·배치명·해상도·작업 상태를 함께 반환. 배치 소유자와 ADMIN 외에 그 이미지를 할당받은 작업자도 조회 가능
   *
   * @param batchId 이미지가 속한 배치 식별자
   * @param imageId 조회할 이미지 식별자
   * @return 이미지 정보와 할당 현황을 합친 응답
   * @throws CustomException 배치나 이미지가 없거나 접근 권한이 없는 경우
   */
  ImageResponse getImage(Long batchId, Long imageId);

  /**
   * 이미지 파일을 저장소에서 읽어 반환
   *
   * <p>브라우저가 &lt;img&gt; 태그로 직접 참조하는 바이너리 응답용
   *
   * <p>width 를 주면 비율을 유지한 채 그 너비로 줄인 JPEG 썸네일을 돌려줌. 결과는 캐시되므로 같은 너비의 두 번째 요청부터는 다시 만들지 않음. width 가
   * null 이면 원본을 그대로 돌려줌
   *
   * @param batchId 이미지가 속한 배치 식별자
   * @param imageId 조회할 이미지 식별자
   * @param width 썸네일 너비 (px). null 이면 원본
   * @return 파일 리소스와 응답 헤더용 메타데이터
   * @throws CustomException 배치나 이미지가 없거나 권한이 없는 경우, 저장소에 실제 파일이 없는 경우
   */
  ImageFile loadImageFile(Long batchId, Long imageId, Integer width);

  /**
   * 배치에 속한 이미지 하나를 삭제
   *
   * <p>DB 레코드와 함께 저장소에 기록된 실제 파일도 정리
   *
   * @param batchId 이미지가 속한 배치 식별자
   * @param imageId 삭제할 이미지 식별자
   * @throws CustomException 배치나 이미지가 없거나 현재 사용자의 배치가 아닌 경우
   */
  void deleteImage(Long batchId, Long imageId);
}
