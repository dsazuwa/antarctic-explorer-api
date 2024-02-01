package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.pojo.ExpeditionFilter;
import com.antarctica.explorer.api.service.ExpeditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
  public ResponseEntity<?> findAllExpeditions(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "6") int size,
      @RequestParam(defaultValue = "cruiseLine") String sort,
      @RequestParam(defaultValue = "asc") String dir,
      @ModelAttribute ExpeditionFilter filter) {
    if (isInvalidSortField(sort))
      return new ResponseEntity<>("Invalid sort field", HttpStatus.BAD_REQUEST);

    try {
      return ResponseEntity.ok(
          service.findAll(
              filter,
              Math.max(0, page),
              Math.max(0, size),
              sort,
              dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC));
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private boolean isInvalidSortField(String sort) {
    return !"name".equalsIgnoreCase(sort)
        && !"cruiseLine".equalsIgnoreCase(sort)
        && !"startingPrice".equalsIgnoreCase(sort);
  }
}
