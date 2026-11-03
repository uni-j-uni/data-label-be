/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.signtelecom.datalabelbe.domain.review.entity.SubmissionSnapshot;

/**
 * 승인 스냅샷 조회·삭제
 *
 * <p>Submission 쪽에 스냅샷 연관을 두지 않음. 외래키가 없는 쪽(mappedBy)의 OneToOne 은 지연 로딩이 되지 않아, 검수 목록처럼 제출을 여러 건 읽을
 * 때마다 스냅샷(payload 포함)을 한 건씩 추가 조회하기 때문
 */
public interface SubmissionSnapshotRepository extends JpaRepository<SubmissionSnapshot, Long> {

  Optional<SubmissionSnapshot> findBySubmissionId(Long submissionId);

  /**
   * 배치에 속한 승인 건의 스냅샷을 제출·이미지와 함께 조회
   *
   * <p>데이터셋 버전 생성에 사용. 이미지당 승인 건은 하나지만 혹시 모를 중복에 대비해 최신 건이 앞에 오도록 정렬
   *
   * @param batchId 배치 식별자
   * @return 승인 스냅샷 목록
   */
  @Query(
      """
      select sn from SubmissionSnapshot sn
      join fetch sn.submission s
      join fetch s.image i
      where i.batch.id = :batchId and s.status = com.signtelecom.datalabelbe.domain.review.entity.SubmissionStatus.APPROVED
      order by i.id asc, s.id desc
      """)
  List<SubmissionSnapshot> findApprovedByBatchId(@Param("batchId") Long batchId);

  /** 이미지 삭제 시 제출보다 먼저 호출. submission_snapshot.submission_id 외래키 때문 */
  @Modifying
  @Query(
      "delete from SubmissionSnapshot sn where sn.submission.id in "
          + "(select s.id from Submission s where s.image.id = :imageId)")
  int deleteByImageId(@Param("imageId") Long imageId);

  /** 배치 삭제 시 제출보다 먼저 호출. submission_snapshot.submission_id 외래키 때문 */
  @Modifying
  @Query(
      "delete from SubmissionSnapshot sn where sn.submission.id in "
          + "(select s.id from Submission s where s.image.batch.id = :batchId)")
  int deleteByBatchId(@Param("batchId") Long batchId);
}
