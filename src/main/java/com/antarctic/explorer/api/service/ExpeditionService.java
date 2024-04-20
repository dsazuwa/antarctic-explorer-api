package com.antarctic.explorer.api.service;

import com.antarctic.explorer.api.model.CruiseLine;
import com.antarctic.explorer.api.model.Expedition;
import com.antarctic.explorer.api.model.Gallery;
import com.antarctic.explorer.api.repository.ExpeditionRepository;
import com.antarctic.explorer.api.repository.GalleryRepository;
import com.antarctic.explorer.api.response.ExpeditionDTO;
import com.antarctic.explorer.api.response.ExpeditionsResponse;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ExpeditionService {

  private final ExpeditionRepository expeditionRepository;
  private final GalleryRepository galleryRepository;

  public ExpeditionService(ExpeditionRepository repository, GalleryRepository galleryRepository) {
    this.expeditionRepository = repository;
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

  public ExpeditionDTO getExpedition(String cName, String name) {
    Map<String, Object> obj = expeditionRepository.getByCruiseLineAndName(cName, name);
    return (!obj.isEmpty()) ? new ExpeditionDTO(obj) : null;
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

  public void saveGalleryImg(Expedition expedition, String photoUrl, String alt) {
    Optional<Gallery> existingGallery =
        galleryRepository.findByExpeditionAndPhotoUrl(expedition, photoUrl);

    if (existingGallery.isEmpty()) galleryRepository.save(new Gallery(expedition, photoUrl, alt));
  }
}
