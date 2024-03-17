package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.model.*;
import com.antarctica.explorer.api.repository.DepartureRepository;
import com.antarctica.explorer.api.response.DeparturesResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DepartureService {
  private final DepartureRepository departureRepository;

  public DepartureService(DepartureRepository departureRepository) {
    this.departureRepository = departureRepository;
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

  public DeparturesResponse findExpeditionDepartures(
      int cruiseLineId, String name, int page, int size, String sortField, Sort.Direction dir) {

    Page<Map<String, Object>> result =
        departureRepository.findExpeditionDepartures(
            PageRequest.of(page, size, Sort.by(dir, sortField)), cruiseLineId, name);

    return new DeparturesResponse(result);
  }
}
