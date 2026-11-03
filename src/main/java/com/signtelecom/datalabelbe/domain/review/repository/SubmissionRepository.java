/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.signtelecom.datalabelbe.domain.review.entity.Submission;
import com.signtelecom.datalabelbe.domain.review.entity.SubmissionStatus;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

  /**
   * 검수 목록을 페이지 단위로 조회
   *
   * <p>목록 화면에 쓰는 이미지·배치·제출자는 fetch join 으로 함께 가져옴. 컬렉션을 fetch join 하지 않으므로 DB 에서 페이징됨
   *
   * @param statuses 포함할 검수 상태. 비어 있으면 안 되므로 전체 조회는 모든 상태를 넘김
   * @param batchId 배치 필터. null 이면 전체 배치
   * @param pageable 페이지와 정렬
   * @return 제출 건 페이지
   */
  @Query(
      value =
          """
          select s from Submission s
          join fetch s.image i
          join fetch i.batch b
          join fetch s.submittedBy u
          where s.status in :statuses and (:batchId is null or b.id = :batchId)
          """,
      countQuery =
          """
          select count(s.id) from Submission s
          where s.status in :statuses and (:batchId is null or s.image.batch.id = :batchId)
          """)
  Page<Submission> search(
      @Param("statuses") Collection<SubmissionStatus> statuses,
      @Param("batchId") Long batchId,
      Pageable pageable);

  /** 검수 상태별 제출 건수. 건이 없는 상태는 결과에 없으므로 호출부에서 0으로 보정해야 함 */
  @Query(
      """
      select s.status as status, count(s.id) as count from Submission s
      where (:batchId is null or s.image.batch.id = :batchId)
      group by s.status
      """)
  List<SubmissionStatusCount> countGroupedByStatus(@Param("batchId") Long batchId);

  /**
   * 배치에 속한 이미지들의 제출 건을 모두 조회
   *
   * <p>배치 삭제 시 이미지보다 먼저 지워야 함. 벌크 delete 대신 엔티티를 지우는 이유는 Comment 가 CASCADE 로 함께 정리되어야 하기 때문.
   * SubmissionSnapshot 은 연관이 없으므로 그 전에 SubmissionSnapshotRepository 로 따로 지워야 함
   *
   * @param batchId 배치 식별자
   * @return 해당 배치 이미지들의 제출 목록
   */
  @Query("select s from Submission s where s.image.batch.id = :batchId")
  List<Submission> findByBatchId(@Param("batchId") Long batchId);

  @Query("select s from Submission s where s.image.id = :imageId")
  List<Submission> findByImageId(@Param("imageId") Long imageId);

  long countByStatus(SubmissionStatus status);

  /** 검수 상태별 건수 집계 결과 프로젝션 */
  interface SubmissionStatusCount {

    SubmissionStatus getStatus();

    Long getCount();
  }
}
