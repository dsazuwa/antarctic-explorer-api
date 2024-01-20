package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
  public MainResponse getData() {
    Map<String, CruiseLine> cruiseLines = cruiseLineService.getCruiseLines();
    List<ExpeditionDTO> expeditions = expeditionService.findAll();

    expeditions.sort(
        Comparator.comparing(ExpeditionDTO::cruiseLine).thenComparing(ExpeditionDTO::name));

    return new MainResponse(cruiseLines, expeditions);
  }

  public record MainResponse(
      Map<String, CruiseLine> cruiseLines, List<ExpeditionDTO> expeditions) {}
}
