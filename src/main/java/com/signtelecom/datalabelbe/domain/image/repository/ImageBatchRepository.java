/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.signtelecom.datalabelbe.domain.image.entity.ImageBatch;

public interface ImageBatchRepository extends JpaRepository<ImageBatch, Long> {

  List<ImageBatch> findAllByOrderByCreatedAtDesc();
}
