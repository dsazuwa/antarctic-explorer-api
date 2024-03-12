package com.antarctica.explorer.api.controller;

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
import org.springframework.web.server.ResponseStatusException;

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
  public ResponseEntity<VesselResponse> getVesselById(@PathVariable String id) {
    try {
      VesselResponse vessel = vesselService.getById(Integer.parseInt(id));

      if (vessel != null) return ResponseEntity.ok(vessel);
      else
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Vessel with ID (" + id + ") not found.");

    } catch (NumberFormatException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID: " + id + '.');
    }
  }
}
