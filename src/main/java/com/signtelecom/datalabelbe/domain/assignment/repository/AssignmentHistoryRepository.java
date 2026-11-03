/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentHistory;

public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {

  /** 이미지 삭제 전 호출. image_id 외래키가 NO ACTION 이라 이력이 남아 있으면 이미지 삭제가 실패 */
  @Modifying
  @Query("delete from AssignmentHistory h where h.image.id = :imageId")
  int deleteByImageId(@Param("imageId") Long imageId);

  /** 배치 삭제 전 호출 */
  @Modifying
  @Query("delete from AssignmentHistory h where h.image.batch.id = :batchId")
  int deleteByBatchId(@Param("batchId") Long batchId);
}
