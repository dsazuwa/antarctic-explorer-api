package com.antarctic.explorer.api.controller;

import com.antarctic.explorer.api.response.DeparturesResponse;
import com.antarctic.explorer.api.response.ExpeditionDTO;
import com.antarctic.explorer.api.response.ExpeditionResponse;
import com.antarctic.explorer.api.service.DepartureService;
import com.antarctic.explorer.api.service.ExpeditionService;
import com.antarctic.explorer.api.response.ExpeditionsResponse;
import com.antarctic.explorer.api.service.ExpeditionFilter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@Validated
public class ExpeditionController {
  private final ExpeditionService expeditionService;
  private final DepartureService departureService;

  @Autowired
  public ExpeditionController(
      ExpeditionService expeditionService, DepartureService departureService) {
    this.expeditionService = expeditionService;
    this.departureService = departureService;
  }

  @GetMapping("/expeditions")
  public ResponseEntity<ExpeditionsResponse> findAllExpeditions(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "6") @Min(1) int size,
      @RequestParam(defaultValue = "nearestDate")
          @Pattern(
              regexp = "name|cruiseLine|nearestDate|startingPrice",
              message = "must be one of 'name', 'cruiseLine', 'startingPrice', or 'nearestDate'.")
          String sort,
      @RequestParam(defaultValue = "asc") String dir,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
      @RequestParam(required = false) String[] cruiseLines,
      @RequestParam(name = "duration.min", defaultValue = "0") @Min(0) Integer durationMin,
      @RequestParam(name = "duration.max", defaultValue = "" + Integer.MAX_VALUE) @Min(0)
          Integer durationMax,
      @RequestParam(name = "capacity.min", required = false) @Min(0) Integer capacityMin,
      @RequestParam(name = "capacity.max", required = false) @Min(0) Integer capacityMax) {

    ExpeditionFilter filter =
        new ExpeditionFilter(
            startDate == null ? null : startDate.toString(),
            endDate == null ? null : endDate.toString(),
            cruiseLines,
            new ExpeditionFilter.RangedFilter(durationMin, durationMax),
            new ExpeditionFilter.RangedFilter(capacityMin, capacityMax));

    return ResponseEntity.ok(
        expeditionService.findAll(
            filter,
            page,
            size,
            sort,
            dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC));
  }

  @GetMapping("/cruise-lines/{cName}/expeditions/{name}")
  public ResponseEntity<ExpeditionDTO> getExpedition(
      @PathVariable @NotBlank String cName,
      @PathVariable @NotBlank String name,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "5") @Min(1) int size,
      @RequestParam(defaultValue = "startDate")
          @Pattern(regexp = "startDate|price", message = "must be one of 'startDate' or 'price'.")
          String sort,
      @RequestParam(defaultValue = "asc") String dir) {

    ExpeditionResponse expedition = expeditionService.getExpedition(cName, name);

    if (expedition == null)
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Expedition (" + name + ") not found.");

    DeparturesResponse departures =
        departureService.getExpeditionDepartures(
            cName,
            name,
            page,
            size,
            sort,
            dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC);

    return ResponseEntity.ok(new ExpeditionDTO(expedition, departures));
  }

  @GetMapping("/cruise-lines/{cName}/expeditions/{name}/departures")
  public ResponseEntity<DeparturesResponse> findExpeditionDepartures(
      @PathVariable @NotBlank String cName,
      @PathVariable @NotBlank String name,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "5") @Min(1) int size,
      @RequestParam(defaultValue = "startDate")
          @Pattern(regexp = "startDate|price", message = "must be one of 'startDate' or 'price'.")
          String sort,
      @RequestParam(defaultValue = "asc") String dir) {

    return ResponseEntity.ok(
        departureService.getExpeditionDepartures(
            cName,
            name,
            page,
            size,
            sort,
            dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC));
  }
}
