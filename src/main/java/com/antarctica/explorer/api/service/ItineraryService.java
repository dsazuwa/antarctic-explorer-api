package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.model.Expedition;
import com.antarctica.explorer.api.model.Itinerary;
import com.antarctica.explorer.api.model.ItineraryDetail;
import com.antarctica.explorer.api.repository.ItineraryDetailRepository;
import com.antarctica.explorer.api.repository.ItineraryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ItineraryService {
  private final ItineraryRepository itineraryRepository;
  private final ItineraryDetailRepository detailRepository;

  public ItineraryService(
      ItineraryRepository itineraryRepository, ItineraryDetailRepository detailRepository) {
    this.itineraryRepository = itineraryRepository;
    this.detailRepository = detailRepository;
  }

  public Itinerary saveItinerary(
      Expedition expedition,
      String name,
      String startPort,
      String endPort,
      String duration,
      String mapUrl) {
    return itineraryRepository.save(
        new Itinerary(expedition, name, startPort, endPort, duration, mapUrl));
  }

  public void saveItineraryDetail(
      Itinerary itinerary, String day, String header, String[] content) {
    detailRepository.save(new ItineraryDetail(itinerary, day, header, content));
  }

  public List<Itinerary> getItinerary(Expedition expedition, String startPort, String endPort) {
    return itineraryRepository.findByExpeditionAndStartPortAndEndPort(
        expedition, startPort, endPort);
  }
}
