package com.tracking.fleet.shared.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "PageResponse", description = "Paginated collection of API resources")
public record PageResponse<T>(
    @Schema(description = "Items on the current page") List<T> content,
    @Schema(description = "Zero-based page index", example = "0") int page,
    @Schema(description = "Requested page size", example = "20") int size,
    @Schema(description = "Total number of matching items", example = "42") long totalElements,
    @Schema(description = "Total number of pages", example = "3") int totalPages,
    @Schema(description = "Whether this is the first page", example = "true") boolean first,
    @Schema(description = "Whether this is the last page", example = "false") boolean last) {

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
