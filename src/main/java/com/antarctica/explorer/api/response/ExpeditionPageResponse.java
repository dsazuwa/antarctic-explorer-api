package com.antarctica.explorer.api.response;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import java.util.List;
import org.springframework.data.domain.Page;

public record ExpeditionPageResponse(
    List<ExpeditionDTO> data, int itemsPerPage, long totalItems, int totalPages, int currentPage) {

  public ExpeditionPageResponse(Page<ExpeditionDTO> page) {
    this(
        page.getContent(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber());
  }
}
