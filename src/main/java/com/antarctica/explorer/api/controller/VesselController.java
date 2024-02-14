package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.response.ErrorResponse;
import com.antarctica.explorer.api.response.VesselResponse;
import com.antarctica.explorer.api.response.VesselsResponse;
import com.antarctica.explorer.api.service.VesselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vessels")
public class VesselController {

  private final VesselService vesselService;

  @Autowired
  public VesselController(VesselService service) {
    this.vesselService = service;
  }

  @GetMapping
  public ResponseEntity<VesselsResponse> getAllVessels() {
    return ResponseEntity.ok(vesselService.getAllVessels());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getVesselById(@PathVariable String id) {
    try {
      VesselResponse vessel = vesselService.getById(Integer.parseInt(id));

      return (vessel != null)
          ? ResponseEntity.ok(vessel)
          : ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new ErrorResponse("Vessel with ID " + id + " not found"));
    } catch (NumberFormatException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("Invalid vessel ID: " + id));
    }
  }
}
