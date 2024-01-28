package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.pojo.response.ExpeditionResponse;
import com.antarctica.explorer.api.service.ExpeditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expeditions")
public class ExpeditionController {
  private final ExpeditionService service;

  @Autowired
  public ExpeditionController(ExpeditionService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<ExpeditionResponse> findAllExpeditions(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size) {
    try {
      return ResponseEntity.ok(service.findAll(page, size));
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
