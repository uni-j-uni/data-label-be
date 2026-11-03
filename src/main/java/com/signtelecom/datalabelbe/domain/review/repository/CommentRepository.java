/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.review.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.signtelecom.datalabelbe.domain.review.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  /**
   * 이미지에 달린 검수 코멘트를 제출 건 구분 없이 최신순으로 조회
   *
   * <p>이미지 하나에 제출이 여러 번 있을 수 있으므로 모든 제출 건의 코멘트를 함께 조회
   *
   * @param imageId 대상 이미지 식별자
   * @return 코멘트 목록 (최신순)
   */
  @Query(
      """
      select c from Comment c
      join fetch c.author a
      join fetch c.submission s
      where s.image.id = :imageId
      order by c.createdAt desc, c.id desc
      """)
  List<Comment> findByImageIdOrderByCreatedAtDesc(@Param("imageId") Long imageId);

  /**
   * 제출 건별 코멘트 수를 한 번의 쿼리로 집계
   *
   * <p>제출마다 count 쿼리를 던지는 N+1 방지. 검수 목록 한 페이지에 실린 제출만 센다. 코멘트가 없는 제출은 결과에 포함되지 않으므로 호출부에서 0으로 보정해야
   * 함
   *
   * @param submissionIds 집계할 제출 식별자 목록
   * @return 제출 식별자와 코멘트 수의 쌍 목록
   */
  @Query(
      "select c.submission.id as submissionId, count(c.id) as commentCount"
          + " from Comment c where c.submission.id in :submissionIds group by c.submission.id")
  List<SubmissionCommentCount> countGroupedBySubmissionIdIn(
      @Param("submissionIds") Collection<Long> submissionIds);

  /** 제출 건별 코멘트 수 집계 결과 프로젝션 */
  interface SubmissionCommentCount {

    Long getSubmissionId();

    Long getCommentCount();
  }
}
