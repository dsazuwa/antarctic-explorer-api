package com.antarctica.explorer.api.response;

import com.antarctica.explorer.api.dto.DepartureDTO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

public record DeparturesResponse(
    List<DepartureDTO> data, int itemsPerPage, long totalItems, int totalPages, int currentPage) {

  public DeparturesResponse(Page<Map<String, Object>> page) {
    this(
        page.getContent().stream().map(DepartureDTO::new).collect(Collectors.toList()),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber());
  }
}
