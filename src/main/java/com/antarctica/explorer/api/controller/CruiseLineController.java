package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.response.CruiseLineDTO;
import com.antarctica.explorer.api.service.CruiseLineService;
import jakarta.validation.constraints.Min;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cruise-lines")
@Validated
public class CruiseLineController {

  private final CruiseLineService service;

  @Autowired
  public CruiseLineController(CruiseLineService service) {
    this.service = service;
  }

  @GetMapping
  public Map<String, CruiseLine> getAllCruiseLines() {
    return service.getCruiseLines();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CruiseLineDTO> getCruiseLineById(@PathVariable @Min(1) int id) {
    CruiseLineDTO cruiseLine = service.getCruiseLine(id);

    if (cruiseLine != null) return ResponseEntity.ok(cruiseLine);
    else
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Cruise line with ID (" + id + ") not found.");
  }
}
