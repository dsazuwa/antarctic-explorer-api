package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.response.MainResponse;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class MainController {
  private final CruiseLineService cruiseLineService;
  private final ExpeditionService expeditionService;

  @Autowired
  public MainController(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    this.cruiseLineService = cruiseLineService;
    this.expeditionService = expeditionService;
  }

  @GetMapping("/api")
  public MainResponse getData(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "6") @Min(1) int size) {

    return new MainResponse(
        cruiseLineService.getCruiseLineNames(),
        expeditionService.findAll(page, size, "nearestDate", Sort.Direction.ASC));
  }
}
