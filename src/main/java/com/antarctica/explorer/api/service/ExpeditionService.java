package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Expedition;
import com.antarctica.explorer.api.pojo.response.ExpeditionResponse;
import com.antarctica.explorer.api.repository.ExpeditionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    return repository.findAll().stream().map(ExpeditionDTO::new).collect(Collectors.toList());
  }

  public ExpeditionResponse findAll(int page, int size) {
    Pageable paging = PageRequest.of(page, size);
    Page<ExpeditionDTO> expeditionPage = repository.findAll(paging).map(ExpeditionDTO::new);
    return new ExpeditionResponse(expeditionPage);
  }

  public ExpeditionResponse findAll(int page, int size, String sortField, Sort.Direction dir) {
    Sort sort =
        sortField.equalsIgnoreCase("cruiseLine")
            ? Sort.by(dir, sortField).and(Sort.by("name"))
            : Sort.by(dir, sortField);

    Pageable paging = PageRequest.of(page, size, sort);
    Page<ExpeditionDTO> expeditionPage = repository.findAll(paging).map(ExpeditionDTO::new);
    return new ExpeditionResponse(expeditionPage);
  }

  public Optional<Expedition> findById(Long id) {
    return repository.findById(id);
  }

  public Optional<Expedition> findByCruiseLineAndName(CruiseLine cruiseLine, String name) {
    return repository.findByCruiseLineAndName(cruiseLine, name);
  }
}
