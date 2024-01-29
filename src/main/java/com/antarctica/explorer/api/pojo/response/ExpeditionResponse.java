package com.antarctica.explorer.api.pojo.response;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import java.util.List;
import org.springframework.data.domain.Page;

public record ExpeditionResponse(
    List<ExpeditionDTO> expeditions,
    int itemsPerPage,
    long totalItems,
    int totalPages,
    int currentPage) {
  public ExpeditionResponse(Page<ExpeditionDTO> page) {
    this(
        page.getContent(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber());
  }
}