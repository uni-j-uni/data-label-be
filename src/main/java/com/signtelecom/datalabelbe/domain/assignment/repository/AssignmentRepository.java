/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.signtelecom.datalabelbe.domain.assignment.entity.Assignment;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentStatus;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

  boolean existsByImageIdAndWorkerId(Long imageId, Long workerId);

  Optional<Assignment> findByImageId(Long imageId);

  /**
   * 이미지 식별자 목록에 해당하는 할당들을 한 번에 조회
   *
   * <p>할당·해제 모두 여러 이미지를 동시에 다루므로, 이미지마다 쿼리를 던지지 않도록 묶어서 조회
   *
   * @param imageIds 조회할 이미지 식별자 목록
   * @return 해당 이미지에 걸린 할당 목록 (할당이 없는 이미지는 결과에 없음)
   */
  @Query(
      "select a from Assignment a join fetch a.image i join fetch a.worker w join fetch a.assignedBy where i.id in :imageIds")
  List<Assignment> findByImageIdIn(@Param("imageIds") Collection<Long> imageIds);

  /** 작업자의 할당 중 주어진 상태인 것만 조회 */
  @EntityGraph(attributePaths = {"image"})
  List<Assignment> findByWorkerIdAndStatusIn(Long workerId, Collection<AssignmentStatus> statuses);

  /**
   * 작업자의 할당을 커서 기반으로 조회 (최신순)
   *
   * <p>할당 식별자가 생성 순서와 같으므로 식별자 역순을 최신순으로 쓰고, 마지막으로 받은 식별자를 커서로 삼음. offset 방식과 달리 목록 중간에 할당이 추가·해제돼도
   * 항목이 중복되거나 빠지지 않음
   *
   * @param workerId 작업자 식별자
   * @param statuses 포함할 상태. 비어 있으면 안 되므로 전체 조회는 모든 상태를 넘김
   * @param cursor 이전 묶음의 마지막 할당 식별자. null 이면 처음부터
   * @param pageable 가져올 개수 (정렬은 쿼리에 고정)
   * @return 할당 목록
   */
  @EntityGraph(attributePaths = {"image", "image.batch", "worker", "assignedBy"})
  @Query(
      """
      select a from Assignment a
      where a.worker.id = :workerId and a.status in :statuses
        and (:cursor is null or a.id < :cursor)
      order by a.id desc
      """)
  List<Assignment> findByWorkerIdBeforeCursor(
      @Param("workerId") Long workerId,
      @Param("statuses") Collection<AssignmentStatus> statuses,
      @Param("cursor") Long cursor,
      Pageable pageable);

  /** 작업자의 할당 중 주어진 상태의 가장 최근 것. '이어서 작업하기' 대상 */
  Optional<Assignment> findFirstByWorkerIdAndStatusInOrderByIdDesc(
      Long workerId, Collection<AssignmentStatus> statuses);

  /** 작업자의 상태별 할당 수. 건이 없는 상태는 결과에 없으므로 호출부에서 0으로 보정해야 함 */
  @Query(
      """
      select a.status as status, count(a.id) as count from Assignment a
      where a.worker.id = :workerId
      group by a.status
      """)
  List<WorkerStatusCount> countGroupedByStatusByWorkerId(@Param("workerId") Long workerId);

  /** 작업자에게 할당된 이미지 중 한 배치에 속한 것의 식별자만 최신순으로 조회. 에디터의 이전/다음 이동용 */
  @Query(
      """
      select a.image.id from Assignment a
      where a.worker.id = :workerId and a.image.batch.id = :batchId
      order by a.id desc
      """)
  List<Long> findImageIdsByWorkerIdAndBatchId(
      @Param("workerId") Long workerId, @Param("batchId") Long batchId);

  @Query(
      """
      select a from Assignment a
      join fetch a.image i
      join fetch i.batch b
      join fetch a.worker w
      join fetch a.assignedBy
      where i.batch.id = :batchId
      order by i.id
      """)
  List<Assignment> findByBatchId(@Param("batchId") Long batchId);

  @Query("select count(a.id) from Assignment a where a.image.batch.id = :batchId")
  long countByBatchId(@Param("batchId") Long batchId);

  @Query(
      "select count(a.id) from Assignment a where a.image.batch.id = :batchId and a.status = :status")
  long countByBatchIdAndStatus(
      @Param("batchId") Long batchId, @Param("status") AssignmentStatus status);

  @Query("delete from Assignment a where a.image.batch.id = :batchId")
  @org.springframework.data.jpa.repository.Modifying
  int deleteByBatchId(@Param("batchId") Long batchId);

  /**
   * 이미지 하나에 걸린 할당을 삭제
   *
   * <p>이미지 삭제 시 먼저 호출해야 함. assignment.image_id 외래키가 NO ACTION 이라 할당이 남아 있으면 이미지 삭제가 제약 위반으로 실패
   *
   * @param imageId 이미지 식별자
   * @return 삭제된 할당 수 (0 또는 1)
   */
  @Query("delete from Assignment a where a.image.id = :imageId")
  @org.springframework.data.jpa.repository.Modifying
  int deleteByImageId(@Param("imageId") Long imageId);

  /**
   * 배치별·상태별 할당 수를 한 번의 쿼리로 집계
   *
   * <p>배치 목록 조회에서 배치마다 count 쿼리를 던지는 N+1을 피하기 위한 메서드. 할당이 없는 배치는 결과에 포함되지 않으므로 호출부에서 0으로 보정해야 함
   *
   * @return 배치 식별자, 상태, 건수의 조합 목록
   */
  @Query(
      """
      select a.image.batch.id as batchId, a.status as status, count(a.id) as count
      from Assignment a
      group by a.image.batch.id, a.status
      """)
  List<BatchStatusCount> countGroupedByBatchIdAndStatus();

  /** 작업자의 상태별 할당 수 집계 결과 프로젝션 */
  interface WorkerStatusCount {

    AssignmentStatus getStatus();

    Long getCount();
  }

  /** 배치별·상태별 할당 수 집계 결과 프로젝션 */
  interface BatchStatusCount {

    Long getBatchId();

    AssignmentStatus getStatus();

    Long getCount();
  }
}
