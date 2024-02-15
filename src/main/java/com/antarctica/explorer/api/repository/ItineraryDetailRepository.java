package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.Itinerary;
import com.antarctica.explorer.api.model.ItineraryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryDetailRepository extends JpaRepository<ItineraryDetail, Integer> {}
