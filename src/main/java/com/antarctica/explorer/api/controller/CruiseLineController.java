package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.dto.CruiseLineDTO;
import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.response.ErrorResponse;
import com.antarctica.explorer.api.service.CruiseLineService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cruiseLines")
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
  public ResponseEntity<?> getCruiseLineById(@PathVariable Long id) {
    CruiseLineDTO cruiseLine = service.getCruiseLine(id);

    if (cruiseLine == null)
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorResponse("Cruise line not found"));

    return ResponseEntity.ok(cruiseLine);
  }
}
