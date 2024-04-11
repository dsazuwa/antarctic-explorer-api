package com.antarctic.explorer.api.service;

import com.antarctic.explorer.api.model.CruiseLine;
import com.antarctic.explorer.api.repository.CruiseLineRepository;
import com.antarctic.explorer.api.repository.ExpeditionRepository;
import com.antarctic.explorer.api.repository.ExtensionRepository;
import com.antarctic.explorer.api.response.CruiseLineDTO;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CruiseLineService {
  private final CruiseLineRepository cruiseLineRepository;
  private final ExpeditionRepository expeditionRepository;
  private final ExtensionRepository extensionRepository;

  public CruiseLineService(
      CruiseLineRepository repository,
      ExpeditionRepository expeditionRepository,
      ExtensionRepository extensionRepository) {
    this.cruiseLineRepository = repository;
    this.expeditionRepository = expeditionRepository;
    this.extensionRepository = extensionRepository;
  }

  public CruiseLine addCruiseLine(CruiseLine cruiseLine) {
    return cruiseLineRepository.save(cruiseLine);
  }

  public CruiseLine saveIfNotExist(CruiseLine cruiseLine) {
    Optional<CruiseLine> existingCruiseLine = cruiseLineRepository.findByName(cruiseLine.getName());

    return existingCruiseLine.orElseGet(() -> cruiseLineRepository.save(cruiseLine));
  }

  @Transactional
  public void deleteExpeditionsAndExtensions(CruiseLine cruiseLine) {
    expeditionRepository.deleteByCruiseLineId(cruiseLine.getId());
    extensionRepository.deleteByCruiseLineId(cruiseLine.getId());
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

  public String[] getCruiseLineNames() {
    return cruiseLineRepository.findAll().stream().map(CruiseLine::getName).toArray(String[]::new);
  }

  public CruiseLineDTO getCruiseLine(int id) {
    Optional<CruiseLine> cruiseLine = cruiseLineRepository.findById(id);
    return cruiseLine
        .map(line -> new CruiseLineDTO(line, expeditionRepository.findAllByCruiseLine(line)))
        .orElse(null);
  }

  public CruiseLine getByName(String name) {
    return cruiseLineRepository.findByName(name).orElseThrow();
  }
}
