package com.tracking.fleet.shared.api.response;

import java.util.List;

/**
 * Paginated API payload.
 *
 * @param content items on the current page
 * @param page zero-based page index
 * @param size requested page size
 * @param totalElements total number of matching items
 * @param totalPages total number of pages
 * @param first whether this is the first page
 * @param last whether this is the last page
 * @param <T> item type
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last) {

  /**
   * Creates a page and defensively copies its content.
   */
  public PageResponse {
    content = List.copyOf(content);
  }

  /**
   * Creates a page response from page content and aggregate metadata.
   *
   * @param content items on the current page
   * @param page zero-based page index
   * @param size requested page size
   * @param totalElements total number of matching items
   * @param <T> item type
   * @return paginated response
   */
  public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
    int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    boolean first = page == 0;
    boolean last = totalPages == 0 || page >= totalPages - 1;

    return new PageResponse<>(content, page, size, totalElements, totalPages, first, last);
  }
}
