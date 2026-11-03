/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.service;

/**
 * 저장소에 기록이 끝난 이미지 파일의 메타데이터
 *
 * @param storagePath 저장소 루트 기준 상대 경로 (batch/{batchId}/{UUID}.{ext})
 * @param contentType 실제 디코딩된 포맷에서 판별한 MIME 타입 (image/jpeg, image/png)
 * @param sizeBytes 파일 크기 (byte)
 * @param width 이미지 너비 (px)
 * @param height 이미지 높이 (px)
 */
public record StoredImage(
    String storagePath, String contentType, long sizeBytes, int width, int height) {}
