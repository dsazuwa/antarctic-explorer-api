package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
}
