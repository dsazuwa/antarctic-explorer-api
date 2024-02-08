package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import com.antarctica.explorer.api.model.*;
import com.antarctica.explorer.api.pojo.ExpeditionFilter;
import com.antarctica.explorer.api.pojo.response.ExpeditionResponse;
import com.antarctica.explorer.api.repository.DepartureRepository;
import com.antarctica.explorer.api.repository.ExpeditionRepository;
import com.antarctica.explorer.api.repository.ItineraryRepository;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
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

  public ExpeditionResponse findAll(
      ExpeditionFilter filter, int page, int size, String sortField, Sort.Direction dir) {

    Sort sort =
        sortField.equalsIgnoreCase("cruiseLine")
            ? Sort.by(dir, sortField).and(Sort.by("name"))
            : Sort.by(dir, sortField);

    Page<Map<String, Object>> x =
        expeditionRepository.findAllExpeditionDTO(
            PageRequest.of(page, size, sort),
            filter.cruiseLines(),
            filter.capacity().min(),
            filter.capacity().max(),
            filter.duration().min(),
            filter.duration().max());

    List<ExpeditionDTO> dto =
        x.getContent().stream().map(this::mapToExpeditionDTO).collect(Collectors.toList());

    return new ExpeditionResponse(
        dto, x.getSize(), x.getTotalElements(), x.getTotalPages(), x.getNumber());
  }

  public ExpeditionResponse findAll(int page, int size, String sortField, Sort.Direction dir) {
    return findAll(new ExpeditionFilter(null, null, null), page, size, sortField, dir);
  }

  private ExpeditionDTO mapToExpeditionDTO(Map<String, Object> resultMap) {
    return new ExpeditionDTO(
        (Integer) resultMap.get("id"),
        (String) resultMap.get("cruise_line"),
        (String) resultMap.get("website"),
        (String) resultMap.get("name"),
        (String) resultMap.get("description"),
        (String) resultMap.get("departing_from"),
        (String) resultMap.get("arriving_at"),
        (String) resultMap.get("duration"),
        (BigDecimal) resultMap.get("starting_price"),
        (Date) resultMap.get("nearest_date"),
        (String) resultMap.get("photo_url"));
  }
}
