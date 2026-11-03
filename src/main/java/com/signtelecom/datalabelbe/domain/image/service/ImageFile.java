/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.service;

import org.springframework.core.io.Resource;

/**
 * 클라이언트로 내려보낼 이미지 파일과 응답 헤더에 필요한 메타데이터
 *
 * @param resource 저장소에서 읽어온 파일 리소스
 * @param contentType MIME 타입 (image/jpeg, image/png)
 * @param sizeBytes 파일 크기 (byte)
 * @param originalFilename 업로드 당시 원본 파일명
 */
public record ImageFile(
    Resource resource, String contentType, long sizeBytes, String originalFilename) {}
