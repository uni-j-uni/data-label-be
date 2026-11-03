/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.service;

/**
 * 배치 목록 집계 값 묶음
 *
 * @param imageCount 배치에 포함된 이미지 수
 * @param assignedCount 작업자에게 할당된 이미지 수
 * @param approvedCount 검수에서 승인된 이미지 수
 * @param pendingReviewCount 검수를 기다리는 이미지 수
 */
public record BatchCounts(
    long imageCount, long assignedCount, long approvedCount, long pendingReviewCount) {

  /** 이미지만 있고 할당이 전혀 없는 배치의 집계 값 */
  public static BatchCounts ofImagesOnly(long imageCount) {
    return new BatchCounts(imageCount, 0L, 0L, 0L);
  }
}
