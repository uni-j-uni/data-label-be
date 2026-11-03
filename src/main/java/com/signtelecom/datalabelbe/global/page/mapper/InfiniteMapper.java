/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.global.page.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.signtelecom.datalabelbe.global.page.response.InfiniteResponse;

@Component
public class InfiniteMapper {

  public <T> InfiniteResponse<T> toInfiniteResponse(
      List<T> content, Long lastCursor, boolean hasNext, int size) {
    return InfiniteResponse.<T>builder()
        .content(content)
        .lastCursor(lastCursor)
        .hasNext(hasNext)
        .size(size)
        .build();
  }
}
