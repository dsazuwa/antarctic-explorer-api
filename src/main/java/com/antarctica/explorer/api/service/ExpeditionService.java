package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.model.*;
import com.antarctica.explorer.api.repository.DepartureRepository;
import com.antarctica.explorer.api.repository.ExpeditionRepository;
import com.antarctica.explorer.api.repository.GalleryRepository;
import com.antarctica.explorer.api.response.DeparturesResponse;
import com.antarctica.explorer.api.response.ExpeditionResponse;
import com.antarctica.explorer.api.response.ExpeditionsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ExpeditionService {
  private final ExpeditionRepository expeditionRepository;
  private final DepartureRepository departureRepository;
  private final GalleryRepository galleryRepository;

  public ExpeditionService(
      ExpeditionRepository repository,
      DepartureRepository departureRepository,
      GalleryRepository galleryRepository) {
    this.expeditionRepository = repository;
    this.departureRepository = departureRepository;
    this.galleryRepository = galleryRepository;
  }

  public Expedition saveIfNotExist(
      CruiseLine cruiseLine,
      String website,
      String name,
      String[] description,
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
        new String[] {description},
        null,
        departingFrom,
        arrivingAt,
        duration,
        startingPrice,
        photoUrl);
  }

  public void saveDeparture(
      Expedition expedition,
      Vessel vessel,
      Itinerary itinerary,
      String name,
      LocalDate startDate,
      LocalDate endDate,
      BigDecimal startingPrice,
      BigDecimal discountedPrice,
      String website) {
    departureRepository.save(
        new Departure(
            expedition,
            vessel,
            itinerary,
            name,
            startDate,
            endDate,
            startingPrice,
            discountedPrice,
            website));
  }

  public void saveGalleryImg(Expedition expedition, String photoUrl, String alt) {
    galleryRepository.save(new Gallery(expedition, photoUrl, alt));
  }

  public ExpeditionResponse getById(int id) {
    Map<String, Object> obj = expeditionRepository.getById(id);
    return (!obj.isEmpty()) ? new ExpeditionResponse(obj) : null;
  }

  public ExpeditionsResponse findAll(
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

    return new ExpeditionsResponse(result);
  }

  public ExpeditionsResponse findAll(int page, int size, String sortField, Sort.Direction dir) {
    return findAll(new ExpeditionFilter(), page, size, sortField, dir);
  }

  public DeparturesResponse findExpeditionDepartures(
      int expeditionId, int page, int size, String sortField, Sort.Direction dir) {

    Page<Map<String, Object>> result =
        departureRepository.findExpeditionDepartures(
            PageRequest.of(page, size, Sort.by(dir, sortField)), expeditionId);

    return new DeparturesResponse(result);
  }
}
