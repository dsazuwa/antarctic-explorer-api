package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.response.ErrorResponse;
import com.antarctica.explorer.api.response.ExpeditionResponse;
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
  public ResponseEntity<?> findAllExpeditions(
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
      return new ResponseEntity<>("Invalid sort field", HttpStatus.BAD_REQUEST);

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
              Math.max(0, size),
              sort,
              dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC));

    } catch (DateTimeParseException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("Invalid date format. Expected format: yyyy-MM-dd"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse(e.getMessage()));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getExpedition(@PathVariable String id) {
    try {
      ExpeditionResponse expedition = expeditionService.getById(Integer.parseInt(id));

      return (expedition != null)
          ? ResponseEntity.ok(expedition)
          : ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new ErrorResponse("Expedition with ID " + id + " not found"));
    } catch (NumberFormatException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("Invalid expedition ID: " + id));
    }
  }

  @GetMapping("/{id}/departures")
  public ResponseEntity<?> findExpeditionDepartures(
      @PathVariable String id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(defaultValue = "startDate") String sort,
      @RequestParam(defaultValue = "asc") String dir) {

    if (!"startDate".equalsIgnoreCase(sort) && !"price".equalsIgnoreCase(sort))
      return new ResponseEntity<>("Invalid sort field", HttpStatus.BAD_REQUEST);

    try {
      return ResponseEntity.ok(
          departureService.findExpeditionDepartures(
              Integer.parseInt(id),
              Math.max(0, page),
              Math.max(0, size),
              sort,
              dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC));
    } catch (NumberFormatException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("Invalid expedition ID: " + id));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse(e.getMessage()));
    }
  }
}
