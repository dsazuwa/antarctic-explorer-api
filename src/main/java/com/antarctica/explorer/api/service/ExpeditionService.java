package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import com.antarctica.explorer.api.model.*;
import com.antarctica.explorer.api.repository.DepartureRepository;
import com.antarctica.explorer.api.repository.ExpeditionRepository;
import com.antarctica.explorer.api.repository.ItineraryRepository;
import com.antarctica.explorer.api.response.ExpeditionPageResponse;
import com.antarctica.explorer.api.response.ExpeditionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

  public Expedition saveIfNotExist(
      CruiseLine cruiseLine,
      String website,
      String name,
      String description,
      String[] highlights,
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
                    highlights,
                    departingFrom,
                    arrivingAt,
                    duration,
                    startingPrice,
                    photoUrl)));
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
    return saveIfNotExist(
        cruiseLine,
        website,
        name,
        description,
        null,
        departingFrom,
        arrivingAt,
        duration,
        startingPrice,
        photoUrl);
  }

  public void saveItinerary(Expedition expedition, String day, String header, String content) {
    itineraryRepository.save(new Itinerary(expedition, day, header, content));
  }

  public void saveDeparture(
      Expedition expedition,
      Vessel vessel,
      String name,
      String departingFrom,
      String arrivingAt,
      LocalDate startDate,
      LocalDate endDate,
      BigDecimal price,
      String website) {
    departureRepository.save(
        new Departure(
            expedition,
            vessel,
            name,
            departingFrom,
            arrivingAt,
            startDate,
            endDate,
            price,
            website));
  }

  public ExpeditionResponse getById(int id) {
    Map<String, Object> obj = expeditionRepository.getById(id);
    return (!obj.isEmpty()) ? new ExpeditionResponse(obj) : null;
  }

  public ExpeditionPageResponse findAll(
      ExpeditionFilter filter, int page, int size, String sortField, Sort.Direction dir) {

    Sort sort =
        sortField.equalsIgnoreCase("cruiseLine")
            ? Sort.by(dir, sortField).and(Sort.by("name"))
            : Sort.by(dir, sortField);

    Page<Map<String, Object>> result =
        expeditionRepository.findAllExpeditionDTO(
            PageRequest.of(page, size, sort),
            filter.startDate(),
            filter.endDate(),
            filter.cruiseLines(),
            filter.capacity().min(),
            filter.capacity().max(),
            filter.duration().min(),
            filter.duration().max());

    return new ExpeditionPageResponse(
        result.getContent().stream().map(ExpeditionDTO::new).collect(Collectors.toList()),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages(),
        result.getNumber());
  }

  public ExpeditionPageResponse findAll(int page, int size, String sortField, Sort.Direction dir) {
    return findAll(new ExpeditionFilter(), page, size, sortField, dir);
  }
}
