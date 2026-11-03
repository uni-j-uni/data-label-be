/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.signtelecom.datalabelbe.domain.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

  List<Image> findByBatchId(Long batchId);

  long countByBatchId(Long batchId);

  /**
   * 배치별 이미지 수를 한 번의 쿼리로 집계
   *
   * <p>배치 목록 조회에서 배치마다 count 쿼리를 던지는 N+1을 피하기 위한 메서드. 이미지가 없는 배치는 결과에 포함되지 않으므로 호출부에서 0으로 보정해야 함
   *
   * @return 배치 식별자와 이미지 수의 쌍 목록
   */
  @Query("select i.batch.id as batchId, count(i.id) as imageCount from Image i group by i.batch.id")
  List<BatchImageCount> countGroupedByBatchId();

  /** 배치별 이미지 수 집계 결과 프로젝션 */
  interface BatchImageCount {

    Long getBatchId();

    Long getImageCount();
  }
}
