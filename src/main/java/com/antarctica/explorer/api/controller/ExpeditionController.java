package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.response.DeparturesResponse;
import com.antarctica.explorer.api.response.ExpeditionResponse;
import com.antarctica.explorer.api.response.ExpeditionsResponse;
import com.antarctica.explorer.api.service.DepartureService;
import com.antarctica.explorer.api.service.ExpeditionFilter;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/expeditions")
public class ExpeditionController {
  private final ExpeditionService expeditionService;
  private final DepartureService departureService;

  @Autowired
  public ExpeditionController(
      ExpeditionService expeditionService, DepartureService departureService) {
    this.expeditionService = expeditionService;
    this.departureService = departureService;
  }

  //  TODO: resolve startDate and endDate not binding when using @ModelAttribute ExpeditionFilter
  @GetMapping
  public ResponseEntity<ExpeditionsResponse> findAllExpeditions(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "6") int size,
      @RequestParam(defaultValue = "nearestDate") String sort,
      @RequestParam(defaultValue = "asc") String dir,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(required = false) String[] cruiseLines,
      @RequestParam(name = "duration.min", defaultValue = "" + Integer.MIN_VALUE)
          Integer durationMin,
      @RequestParam(name = "duration.max", defaultValue = "" + Integer.MAX_VALUE)
          Integer durationMax,
      @RequestParam(name = "capacity.min", required = false) Integer capacityMin,
      @RequestParam(name = "capacity.max", required = false) Integer capacityMax) {

    if (!"name".equalsIgnoreCase(sort)
        && !"cruiseLine".equalsIgnoreCase(sort)
        && !"startingPrice".equalsIgnoreCase(sort)
        && !"nearestDate".equalsIgnoreCase(sort))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Invalid sort field. Must be one of 'name', 'cruiseLine', 'startingPrice', or 'nearestDate'.");

    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      ExpeditionFilter filter =
          new ExpeditionFilter(
              startDate == null ? null : LocalDate.parse(startDate, formatter).toString(),
              endDate == null ? null : LocalDate.parse(endDate, formatter).toString(),
              cruiseLines,
              new ExpeditionFilter.RangedFilter(durationMin, durationMax),
              new ExpeditionFilter.RangedFilter(capacityMin, capacityMax));

      return ResponseEntity.ok(
          expeditionService.findAll(
              filter,
              Math.max(0, page),
              Math.max(1, size),
              sort,
              dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC));

    } catch (DateTimeParseException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Invalid date format. Expected format: yyyy-MM-dd.");
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ExpeditionResponse> getExpedition(@PathVariable String id) {
    try {
      ExpeditionResponse expedition = expeditionService.getById(Integer.parseInt(id));

      if (expedition != null) return ResponseEntity.ok(expedition);
      else
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Expedition with ID (" + id + ") not found.");

    } catch (NumberFormatException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID: " + id + '.');
    }
  }

  @GetMapping("/{id}/departures")
  public ResponseEntity<DeparturesResponse> findExpeditionDepartures(
      @PathVariable String id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(defaultValue = "startDate") String sort,
      @RequestParam(defaultValue = "asc") String dir) {

    if (!"startDate".equalsIgnoreCase(sort) && !"price".equalsIgnoreCase(sort))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Invalid sort field. Must be one of 'startDate' or 'price'.");

    try {
      return ResponseEntity.ok(
          departureService.findExpeditionDepartures(
              Integer.parseInt(id),
              Math.max(0, page),
              Math.max(1, size),
              sort,
              dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC));
    } catch (NumberFormatException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID: " + id + '.');
    }
  }
}
