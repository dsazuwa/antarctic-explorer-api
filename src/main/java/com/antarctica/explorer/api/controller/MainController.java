package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.pojo.response.MainResponse;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {
    Map<String, CruiseLine> cruiseLines = cruiseLineService.getCruiseLines();
    return new MainResponse(
        cruiseLines,
        expeditionService.findAll(
            Math.max(0, page), Math.max(0, size), "cruiseLine", Sort.Direction.ASC));
  }
}
