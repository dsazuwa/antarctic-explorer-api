package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expeditions")
public class ExpeditionController {
  private final ExpeditionService service;

  @Autowired
  public ExpeditionController(ExpeditionService service) {
    this.service = service;
  }

  @GetMapping
  public List<ExpeditionDTO> findAllExpeditions() {
    return service.findAll();
  }
}
