/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.label.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.signtelecom.datalabelbe.domain.label.entity.PoseLabel;

public interface PoseLabelRepository extends JpaRepository<PoseLabel, Long> {

  Optional<PoseLabel> findByImageId(Long imageId);

  /**
   * 이미지 라벨의 키포인트를 한 번에 삭제
   *
   * <p>임시 저장의 전량 교체용. orphanRemoval 로 지우면 키포인트 한 행마다 delete 가 나가므로(13명이면 221회) 벌크로 지움. 객체보다 먼저 호출해야
   * keypoint.pose_object_id 외래키에 걸리지 않음
   */
  @Modifying(flushAutomatically = true)
  @Query(
      "delete from Keypoint k where k.poseObject.id in "
          + "(select o.id from PoseObject o where o.label.image.id = :imageId)")
  int deleteKeypointsByImageId(@Param("imageId") Long imageId);

  /** 이미지 라벨의 인물 객체를 한 번에 삭제. {@link #deleteKeypointsByImageId} 다음에 호출 */
  @Modifying(flushAutomatically = true)
  @Query(
      "delete from PoseObject o where o.label.id in "
          + "(select l.id from PoseLabel l where l.image.id = :imageId)")
  int deleteObjectsByImageId(@Param("imageId") Long imageId);

  /** 라벨이 한 번이라도 저장됐는지 확인. 객체 0개로 저장한 경우도 true */
  boolean existsByImageId(Long imageId);

  /**
   * 주어진 이미지 중 라벨이 저장된 이미지의 식별자만 조회
   *
   * <p>객체 수가 아니라 라벨 행 존재 여부로 판단하므로 객체 0개로 저장한 이미지도 포함
   *
   * @param imageIds 확인할 이미지 식별자 목록
   * @return 라벨이 저장된 이미지 식별자 목록
   */
  @Query("select l.image.id from PoseLabel l where l.image.id in :imageIds")
  List<Long> findLabeledImageIdsIn(@Param("imageIds") Collection<Long> imageIds);

  /** 배치에 속한 이미지 중 라벨이 저장된 이미지의 식별자만 조회 */
  @Query("select l.image.id from PoseLabel l where l.image.batch.id = :batchId")
  List<Long> findLabeledImageIdsByBatchId(@Param("batchId") Long batchId);

  /**
   * 배치에 속한 이미지들의 라벨을 모두 조회
   *
   * <p>배치 삭제 시 이미지보다 먼저 지워야 하므로, 대상을 한 번에 가져옴. 벌크 delete 대신 엔티티를 지우는 이유는 PoseObject/Keypoint가
   * CASCADE로 함께 정리되어야 하기 때문
   *
   * @param batchId 배치 식별자
   * @return 해당 배치 이미지들의 라벨 목록
   */
  @Query("select l from PoseLabel l where l.image.batch.id = :batchId")
  List<PoseLabel> findByBatchId(@Param("batchId") Long batchId);

  /**
   * 배치별 인물 객체 수를 한 번의 쿼리로 집계
   *
   * <p>이미지마다 count 쿼리를 던지는 N+1 방지. 객체가 없는 이미지는 결과에 포함되지 않으므로 호출부에서 0으로 보정해야 함
   *
   * @param batchId 배치 식별자
   * @return 이미지 식별자와 객체 수의 쌍 목록
   */
  @Query(
      """
      select l.image.id as imageId, count(o.id) as objectCount
      from PoseLabel l join l.objects o
      where l.image.batch.id = :batchId
      group by l.image.id
      """)
  List<ImageObjectCount> countObjectsGroupedByImageId(@Param("batchId") Long batchId);

  @Query("select count(o.id) from PoseLabel l join l.objects o where l.image.id = :imageId")
  long countObjectsByImageId(@Param("imageId") Long imageId);

  /**
   * 주어진 이미지들의 인물 객체 수를 한 번의 쿼리로 집계
   *
   * <p>배치 단위가 아닌 임의의 이미지 묶음 집계용. 객체가 없는 이미지는 결과에 포함되지 않으므로 호출부에서 0으로 보정해야 함
   *
   * @param imageIds 집계할 이미지 식별자 목록
   * @return 이미지 식별자와 객체 수의 쌍 목록
   */
  @Query(
      """
      select l.image.id as imageId, count(o.id) as objectCount
      from PoseLabel l join l.objects o
      where l.image.id in :imageIds
      group by l.image.id
      """)
  List<ImageObjectCount> countObjectsGroupedByImageIdIn(
      @Param("imageIds") Collection<Long> imageIds);

  /** 이미지별 인물 객체 수 집계 결과 프로젝션 */
  interface ImageObjectCount {

    Long getImageId();

    Long getObjectCount();
  }
}
