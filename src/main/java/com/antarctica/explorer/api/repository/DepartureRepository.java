package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.Departure;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartureRepository extends JpaRepository<Departure, Long> {
  @Query(
      value =
          """
            WITH d AS (
              SELECT
                d.departure_id AS id,
                d.name,
                i.name AS itinerary,
                v.name AS vessel,
                i.departing_from,
                i.arriving_at,
                i.duration,
                d.start_date,
                d.end_date,
                d.starting_price,
                d.discounted_price,
                d.website
              FROM antarctica.departures d
              JOIN antarctica.vessels v ON v.vessel_id = d.vessel_id
              JOIN antarctica.itineraries i ON i.itinerary_id = d.itinerary_id
              WHERE d.expedition_id = :p_expedition_id AND d.starting_price IS DISTINCT FROM NULL
            )
            SELECT * FROM d
          """,
      countQuery =
          """
            WITH d AS (
              SELECT
                d.departure_id AS id,
                d.name,
                i.name AS itinerary,
                v.name AS vessel,
                i.departing_from,
                i.arriving_at,
                i.duration,
                d.start_date,
                d.end_date,
                d.starting_price,
                d.discounted_price,
                d.website
              FROM antarctica.departures d
              JOIN antarctica.vessels v ON v.vessel_id = d.vessel_id
              JOIN antarctica.itineraries i ON i.itinerary_id = d.itinerary_id
              WHERE d.expedition_id = :p_expedition_id AND d.starting_price IS DISTINCT FROM NULL
            )
            SELECT * FROM d
          """,
      nativeQuery = true,
      queryRewriter = ExpeditionQueryWriter.class)
  Page<Map<String, Object>> findExpeditionDepartures(
      Pageable pageable, @Param("p_expedition_id") int id);
}
