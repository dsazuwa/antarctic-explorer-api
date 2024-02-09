package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.pojo.ExpeditionFilter;
import com.antarctica.explorer.api.response.ErrorResponse;
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
  private final ExpeditionService service;

  @Autowired
  public ExpeditionController(ExpeditionService service) {
    this.service = service;
  }

  //  TODO: resolve startDate and endDate not binding when using @ModelAttribute ExpeditionFilter
  @GetMapping
  public ResponseEntity<?> findAllExpeditions(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "6") int size,
      @RequestParam(defaultValue = "cruiseLine") String sort,
      @RequestParam(defaultValue = "asc") String dir,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(required = false) String[] cruiseLines,
      @RequestParam(name = "duration.min", required = false) Integer durationMin,
      @RequestParam(name = "duration.max", required = false) Integer durationMax,
      @RequestParam(name = "capacity.min", required = false) Integer capacityMin,
      @RequestParam(name = "capacity.max", required = false) Integer capacityMax) {

    if (isInvalidSortField(sort))
      return new ResponseEntity<>("Invalid sort field", HttpStatus.BAD_REQUEST);

    System.out.println(startDate + " " + endDate);

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
          service.findAll(
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

  private boolean isInvalidSortField(String sort) {
    return !"name".equalsIgnoreCase(sort)
        && !"cruiseLine".equalsIgnoreCase(sort)
        && !"startingPrice".equalsIgnoreCase(sort)
        && !"nearestDate".equalsIgnoreCase(sort);
  }
}
