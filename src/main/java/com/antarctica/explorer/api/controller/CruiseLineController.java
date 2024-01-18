package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.dto.CruiseLineDTO;
import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.service.CruiseLineService;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
  public Map<String, CruiseLine> getAllCruiseLines() {
    return service.getCruiseLines();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CruiseLineByIdResponse> getCruiseLineById(@PathVariable Long id) {
    Optional<CruiseLineDTO> cruiseLine = service.getCruiseLine(id);

    return cruiseLine
        .map(line -> ResponseEntity.ok(new CruiseLineByIdResponse(line, line.name() + " found")))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        new CruiseLineByIdResponse(null, "Cruise line not found with ID: " + id)));
  }

  @GetMapping("/search")
  public ResponseEntity<CruiseLine> getCruiseLineByName(@RequestParam("name") String name) {
    Optional<CruiseLine> cruiseLine = service.findByName(name);
    return cruiseLine.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  public record CruiseLineByIdResponse(CruiseLineDTO cruiseLine, String message) {}
}
