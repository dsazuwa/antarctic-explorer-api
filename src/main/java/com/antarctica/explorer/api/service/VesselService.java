package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Vessel;
import com.antarctica.explorer.api.repository.VesselRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VesselService {
  private final VesselRepository vesselRepository;

  public VesselService(VesselRepository vesselRepository) {
    this.vesselRepository = vesselRepository;
  }

  public Vessel saveIfNotExist(
      CruiseLine cruiseLine, String name, int capacity, String website, String photoUrl) {
    Optional<Vessel> existingVessel = vesselRepository.findByName(name);

    return existingVessel.orElseGet(
        () -> vesselRepository.save(new Vessel(cruiseLine, name, capacity, website, photoUrl)));
  }

  public Vessel getByName(String name) {
    return vesselRepository.findByName(name).orElseThrow();
  }
}
