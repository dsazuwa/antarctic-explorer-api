package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.service.CruiseLineService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cruise-lines")
public class CruiseLineController {
  private final CruiseLineService service;

  @Autowired
  public CruiseLineController(CruiseLineService service) {
    this.service = service;
  }

  @GetMapping
  public List<CruiseLine> getAllCruiseLines() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CruiseLine> getCruiseLineById(@PathVariable Long id) {
    Optional<CruiseLine> cruiseLine = service.findById(id);
    return cruiseLine.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/search")
  public ResponseEntity<CruiseLine> getCruiseLineByName(@RequestParam("name") String name) {
    Optional<CruiseLine> cruiseLine = service.findByName(name);
    return cruiseLine.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }
}
