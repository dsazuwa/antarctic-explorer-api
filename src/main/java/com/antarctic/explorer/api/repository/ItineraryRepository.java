package com.antarctic.explorer.api.repository;

import com.antarctic.explorer.api.model.Expedition;
import com.antarctic.explorer.api.model.Itinerary;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
  List<Itinerary> findByExpedition(Expedition expedition);

  List<Itinerary> findByExpeditionAndName(Expedition expedition, String name);

  List<Itinerary> findByExpeditionAndStartPortAndEndPort(
      Expedition expedition, String startPort, String endPort);
}
