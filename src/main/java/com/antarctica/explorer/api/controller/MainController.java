package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.pojo.response.MainResponse;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size) {
    Map<String, CruiseLine> cruiseLines = cruiseLineService.getCruiseLines();

//    expeditions.sort(
//        Comparator.comparing(ExpeditionDTO::cruiseLine).thenComparing(ExpeditionDTO::name));

    return new MainResponse(cruiseLines, expeditionService.findAll(page, size));
  }
}
