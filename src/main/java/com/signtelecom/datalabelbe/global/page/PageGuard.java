/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.global.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.signtelecom.datalabelbe.global.exception.ErrorGuard;
import com.signtelecom.datalabelbe.global.page.exception.PageErrorStatus;

/**
 * 페이지 요청 값 검증과 변환
 *
 * <p>클라이언트는 페이지 번호를 1부터 보내고({@link
 * com.signtelecom.datalabelbe.global.page.response.PageResponse#getPageNum()} 와 같은 기준), Spring Data
 * 는 0부터 세므로 여기서 맞춘다.
 */
public final class PageGuard {

  /** 한 번에 요청할 수 있는 최대 개수. 목록 전체를 한 번에 내려받는 것을 막기 위함 */
  public static final int MAX_SIZE = 100;

  private PageGuard() {}

  /** 1부터 시작하는 페이지 번호와 크기를 검증해 Pageable 로 변환 */
  public static Pageable pageOf(int page, int size, Sort sort) {
    ErrorGuard.throwIf(page < 1, PageErrorStatus.PAGE_NOT_FOUND);
    return PageRequest.of(page - 1, sizeOf(size), sort);
  }

  /** 페이지 크기가 1 이상 {@link #MAX_SIZE} 이하인지 검증 */
  public static int sizeOf(int size) {
    ErrorGuard.throwIf(size < 1 || size > MAX_SIZE, PageErrorStatus.PAGE_SIZE_ERROR);
    return size;
  }
}
