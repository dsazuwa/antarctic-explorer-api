package com.antarctica.explorer.api.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

public record ExpeditionsResponse(
    List<ExpeditionDTO> data, int itemsPerPage, long totalItems, int totalPages, int currentPage) {

  public ExpeditionsResponse(Page<Map<String, Object>> page) {
    this(
        page.getContent().stream().map(ExpeditionDTO::new).collect(Collectors.toList()),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber());
  }
}
