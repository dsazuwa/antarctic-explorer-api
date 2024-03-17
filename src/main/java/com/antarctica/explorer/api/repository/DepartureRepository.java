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
                COALESCE(i.name, 'Itinerary ' || i.itinerary_id) AS itinerary,
                v.name AS vessel,
                i.departing_from,
                i.arriving_at,
                i.duration,
                d.start_date,
                d.end_date,
                d.starting_price,
                d.discounted_price,
                COALESCE(d.discounted_price, d.starting_price) AS price,
                d.website
              FROM antarctica.departures d
              JOIN antarctica.expeditions e ON e.expedition_id = d.expedition_id
              JOIN antarctica.cruise_lines c ON c.cruise_line_id = e.cruise_line_id
              JOIN antarctica.vessels v ON v.vessel_id = d.vessel_id
              JOIN antarctica.itineraries i ON i.itinerary_id = d.itinerary_id
              WHERE c.cruise_line_id = :p_cruise_line_id AND e.name = :p_name AND d.starting_price IS DISTINCT FROM NULL
            )
            SELECT * FROM d
          """,
      countQuery =
          """
            WITH d AS (
              SELECT
                d.departure_id AS id,
                d.name,
                COALESCE(i.name, 'Itinerary ' || i.itinerary_id) AS itinerary,
                v.name AS vessel,
                i.departing_from,
                i.arriving_at,
                i.duration,
                d.start_date,
                d.end_date,
                d.starting_price,
                d.discounted_price,
                COALESCE(d.discounted_price, d.starting_price) AS price,
                d.website
              FROM antarctica.departures d
              JOIN antarctica.expeditions e ON e.expedition_id = d.expedition_id
              JOIN antarctica.cruise_lines c ON c.cruise_line_id = e.cruise_line_id
              JOIN antarctica.vessels v ON v.vessel_id = d.vessel_id
              JOIN antarctica.itineraries i ON i.itinerary_id = d.itinerary_id
              WHERE c.cruise_line_id = :p_cruise_line_id AND e.name = :p_name AND d.starting_price IS DISTINCT FROM NULL
            )
            SELECT * FROM d
          """,
      nativeQuery = true,
      queryRewriter = ExpeditionQueryWriter.class)
  Page<Map<String, Object>> findExpeditionDepartures(
      Pageable pageable, @Param("p_cruise_line_id") int id, @Param("p_name") String name);
}
