/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.version.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.signtelecom.datalabelbe.domain.version.entity.DatasetVersion;

public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, Long> {

  @Query(
      """
      select v from DatasetVersion v
      join fetch v.batch b
      join fetch v.createdBy u
      order by v.createdAt desc
      """)
  List<DatasetVersion> findAllWithBatchAndCreator();

  List<DatasetVersion> findByBatchId(Long batchId);

  long countByBatchId(Long batchId);
}
