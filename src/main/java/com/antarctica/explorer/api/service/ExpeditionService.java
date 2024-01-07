package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Expedition;
import com.antarctica.explorer.api.repository.ExpeditionRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ExpeditionService {
  private final ExpeditionRepository repository;

  public ExpeditionService(ExpeditionRepository repository) {
    this.repository = repository;
  }

  public Expedition save(Expedition expedition) {
    return repository.save(expedition);
  }

  public Expedition saveIfNotExist(
      CruiseLine cruiseLine,
      String website,
      String name,
      String description,
      String departingFrom,
      String arrivingAt,
      String duration,
      BigDecimal startingPrice,
      String photoUrl) {
    Optional<Expedition> existingExpedition = repository.findByCruiseLineAndName(cruiseLine, name);

    return existingExpedition.orElseGet(
        () ->
            repository.save(
                new Expedition(
                    cruiseLine,
                    website,
                    name,
                    description,
                    departingFrom,
                    arrivingAt,
                    duration,
                    startingPrice,
                    photoUrl)));
  }

  public List<ExpeditionDTO> findAll() {
    List<Expedition> expeditions = repository.findAll();
    List<ExpeditionDTO> expeditionDTOs = new ArrayList<>();

    for (Expedition expedition : expeditions) expeditionDTOs.add(new ExpeditionDTO(expedition));

    return expeditionDTOs;
  }

  public Optional<Expedition> findById(Long id) {
    return repository.findById(id);
  }

  public Optional<Expedition> findByCruiseLineAndName(CruiseLine cruiseLine, String name) {
    return repository.findByCruiseLineAndName(cruiseLine, name);
  }
}
