package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.dto.CruiseLineDTO;
import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.repository.CruiseLineRepository;
import com.antarctica.explorer.api.repository.ExpeditionRepository;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CruiseLineService {
  private final CruiseLineRepository cruiseLineRepository;
  private final ExpeditionRepository expeditionRepository;

  public CruiseLineService(
      CruiseLineRepository repository, ExpeditionRepository expeditionRepository) {
    this.cruiseLineRepository = repository;
    this.expeditionRepository = expeditionRepository;
  }

  public CruiseLine addCruiseLine(CruiseLine cruiseLine) {
    return cruiseLineRepository.save(cruiseLine);
  }

  public CruiseLine saveIfNotExist(
      String name, String website, String expeditionWebsite, String logo) {
    Optional<CruiseLine> existingCruiseLine = cruiseLineRepository.findByName(name);

    return existingCruiseLine.orElseGet(
        () -> cruiseLineRepository.save(new CruiseLine(name, website, expeditionWebsite, logo)));
  }

  public List<CruiseLine> getAll() {
    return cruiseLineRepository.findAll();
  }

  public Map<String, CruiseLine> getCruiseLines() {
    return cruiseLineRepository.findAll().stream()
        .collect(
            Collectors.toMap(
                CruiseLine::getName,
                Function.identity(),
                (existing, replacement) -> existing,
                TreeMap::new));
  }

  public CruiseLineDTO getCruiseLine(Long id) {
    Optional<CruiseLine> cruiseLine = cruiseLineRepository.findById(id);
    return cruiseLine
        .map(line -> new CruiseLineDTO(line, expeditionRepository.findAllByCruiseLine(line)))
        .orElse(null);
  }

  public CruiseLine getByName(String name) {
    return cruiseLineRepository.findByName(name).orElseThrow();
  }
}
