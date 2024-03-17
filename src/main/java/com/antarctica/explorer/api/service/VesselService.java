package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Vessel;
import com.antarctica.explorer.api.repository.VesselRepository;
import com.antarctica.explorer.api.response.VesselResponse;
import com.antarctica.explorer.api.response.VesselsResponse;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class VesselService {
  private final VesselRepository vesselRepository;

  public VesselService(VesselRepository vesselRepository) {
    this.vesselRepository = vesselRepository;
  }

  public Vessel save(
      CruiseLine cruiseLine,
      String name,
      String[] description,
      Integer capacity,
      Integer cabins,
      String website,
      String photoUrl) {

    return vesselRepository.save(
        new Vessel(cruiseLine, name, description, capacity, cabins, website, photoUrl));
  }

  public Vessel saveIfNotExist(
      CruiseLine cruiseLine,
      String name,
      String[] description,
      Integer capacity,
      Integer cabins,
      String website,
      String photoUrl) {
    Optional<Vessel> existingVessel = vesselRepository.findByName(name);

    return existingVessel.orElseGet(
        () ->
            vesselRepository.save(
                new Vessel(cruiseLine, name, description, capacity, cabins, website, photoUrl)));
  }

  public VesselsResponse getAllVessels() {
    return new VesselsResponse(
        vesselRepository.getAllVessels().stream()
            .map(VesselResponse::new)
            .collect(Collectors.toList()));
  }

  public VesselResponse getById(int id) {
    Map<String, Object> obj = vesselRepository.getById(id);
    return (!obj.isEmpty()) ? new VesselResponse(obj) : null;
  }

  public Vessel getByName(String name) {
    return vesselRepository.findByName(name).orElseThrow();
  }

  public Optional<Vessel> findByName(String name) {
    return vesselRepository.findByName(name);
  }

  public Optional<Vessel> findOneByCruiseLIne(CruiseLine cruiseLine) {
    return vesselRepository.findTopByCruiseLineOrderById(cruiseLine);
  }
}
