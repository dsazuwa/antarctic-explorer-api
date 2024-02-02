package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import com.antarctica.explorer.api.model.*;
import com.antarctica.explorer.api.pojo.ExpeditionFilter;
import com.antarctica.explorer.api.pojo.response.ExpeditionResponse;
import com.antarctica.explorer.api.repository.DepartureRepository;
import com.antarctica.explorer.api.repository.ExpeditionRepository;
import com.antarctica.explorer.api.repository.ItineraryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ExpeditionService {
  private final ExpeditionRepository expeditionRepository;
  private final ItineraryRepository itineraryRepository;
  private final DepartureRepository departureRepository;

  public ExpeditionService(
      ExpeditionRepository repository,
      ItineraryRepository itineraryRepository,
      DepartureRepository departureRepository) {
    this.expeditionRepository = repository;
    this.itineraryRepository = itineraryRepository;
    this.departureRepository = departureRepository;
  }

  public Expedition save(Expedition expedition) {
    return expeditionRepository.save(expedition);
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
    Optional<Expedition> existingExpedition =
        expeditionRepository.findByCruiseLineAndName(cruiseLine, name);

    return existingExpedition.orElseGet(
        () ->
            expeditionRepository.save(
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

  public void saveItinerary(Expedition expedition, String day, String header, String content) {
    itineraryRepository.save(new Itinerary(expedition, day, header, content));
  }

  public void saveDeparture(
      Expedition expedition,
      String name,
      String departingFrom,
      String arrivingAt,
      LocalDate startDate,
      LocalDate endDate,
      BigDecimal price,
      String website) {
    departureRepository.save(
        new Departure(
            expedition, name, departingFrom, arrivingAt, startDate, endDate, price, website));
  }

  public List<ExpeditionDTO> findAll() {
    return expeditionRepository.findAll().stream()
        .map(ExpeditionDTO::new)
        .collect(Collectors.toList());
  }

  public ExpeditionResponse findAll(int page, int size) {
    Pageable paging = PageRequest.of(page, size);
    Page<ExpeditionDTO> expeditionPage =
        expeditionRepository.findAll(paging).map(ExpeditionDTO::new);
    return new ExpeditionResponse(expeditionPage);
  }

  public ExpeditionResponse findAll(int page, int size, String sortField, Sort.Direction dir) {
    Sort sort =
        sortField.equalsIgnoreCase("cruiseLine")
            ? Sort.by(dir, sortField).and(Sort.by("name"))
            : Sort.by(dir, sortField);

    Pageable paging = PageRequest.of(page, size, sort);
    Page<ExpeditionDTO> expeditionPage =
        expeditionRepository.findAll(paging).map(ExpeditionDTO::new);
    return new ExpeditionResponse(expeditionPage);
  }

  public ExpeditionResponse findAll(
      ExpeditionFilter filter, int page, int size, String sortField, Sort.Direction dir) {
    Specification<Expedition> spec = ExpeditionSpec.filterBy(filter);
    Sort sort =
        sortField.equalsIgnoreCase("cruiseLine")
            ? Sort.by(dir, sortField).and(Sort.by("name"))
            : Sort.by(dir, sortField);

    Pageable paging = PageRequest.of(page, size, sort);
    Page<ExpeditionDTO> expeditionPage =
        expeditionRepository.findAll(spec, paging).map(ExpeditionDTO::new);
    return new ExpeditionResponse(expeditionPage);
  }

  public Optional<Expedition> findById(Long id) {
    return expeditionRepository.findById(id);
  }

  public Optional<Expedition> findByCruiseLineAndName(CruiseLine cruiseLine, String name) {
    return expeditionRepository.findByCruiseLineAndName(cruiseLine, name);
  }
}
