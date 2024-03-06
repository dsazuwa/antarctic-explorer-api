package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Expedition;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpeditionRepository extends JpaRepository<Expedition, Long> {
  @Query(
      value =
          """
            WITH vessels AS (
              SELECT
                v.vessel_id,
                v.name,
                jsonb_build_object(
                  'id', v.vessel_id,
                  'name', v.name,
                  'description', v.description,
                  'cabins', v.cabins,
                  'capacity', v.capacity,
                  'photo_url', v.photo_url,
                  'website', v.website
                ) AS vessel
              FROM antarctica.vessels v
            ),
            itineraries AS (
              SELECT
                i.itinerary_id,
                i.expedition_id,
                COALESCE(i.name, 'Itinerary #' || i.itinerary_id) AS name,
                i.departing_from,
                i.arriving_at,
                i.duration,
                i.map_url,
                json_agg(DISTINCT jsonb_build_object(
                  'id', d.detail_id,
                  'day', d.day,
                  'header', d.header,
                  'content', d.content
                )) AS schedule
              FROM antarctica.itineraries i
              JOIN antarctica.itinerary_details d ON d.itinerary_id = i.itinerary_id
              GROUP BY i.itinerary_id
            ),
            departures AS (
              SELECT
                i.itinerary_id,
                d.departure_id,
                d.vessel_id,
                jsonb_build_object(
                  'start_date', d.start_date,
                  'end_date', d.end_date
                ) AS departures
              FROM itineraries i
              LEFT JOIN antarctica.departures d ON d.itinerary_id = i.itinerary_id
              LEFT JOIN vessels v ON v.vessel_id = d.vessel_id
              WHERE d.starting_price IS NOT NULL
            ),
            extensions AS (
              SELECT
                exp.expedition_id,
                jsonb_build_object(
                  'name', ext.name,
                  'duration', ext.duration,
                  'starting_price', ext.starting_price,
                  'photo_url', ext.photo_url,
                  'website', ext.website
                ) AS extension
              FROM antarctica.expeditions_extensions ee
              JOIN antarctica.extensions ext ON ext.extension_id = ee.extension_id
              JOIN antarctica.expeditions exp ON exp.expedition_id = ee.expedition_id
            ),
            expeditions AS (
              SELECT
                c.cruise_line_id,
                jsonb_build_object(
                  'id', e.expedition_id,
                  'logo', c.logo,
                  'cruise_line', c.name,
                  'name', e.name,
                  'duration', e.duration,
                  'nearest_date', min(d.start_date),
                  'starting_price', e.starting_price,
                  'photo_url', e.photo_url
                ) AS expedition
              FROM antarctica.expeditions e
              JOIN antarctica.cruise_lines c ON c.cruise_line_id = e.cruise_line_id
              LEFT JOIN (SELECT * FROM antarctica.departures d) d ON e.expedition_id = d.expedition_id
              WHERE c.cruise_line_id =
                (
                  SELECT a.cruise_line_id
                  FROM antarctica.expeditions a
                  WHERE a.expedition_id = :p_expedition_id
                )
              GROUP BY e.expedition_id, c.cruise_line_id
              LIMIT 3
            )
            SELECT
              e.expedition_id AS id,
              e.name,
              e.description,
              e.highlights,
              e.duration,
              e.starting_price,
              e.website,
              e.photo_url,
              jsonb_build_object(
                'name', c.name,
                'logo', c.logo
              ) AS cruise_line,
              json_agg(DISTINCT jsonb_build_object(
                'id', g.photo_id,
                'url', g.photo_url,
                'alt', g.alt
              )) AS gallery,
              json_agg(DISTINCT v.vessel) AS vessels,
              json_agg(DISTINCT jsonb_build_object(
                'id', i.itinerary_id,
                'name', i.name,
                'start_port', i.departing_from,
                'end_port', i.arriving_at,
                'duration', i.duration,
                'map_url', i.map_url,
                'schedule', i.schedule
              )) AS itineraries,
              jsonb_agg(DISTINCT d.departures) AS departures,
              jsonb_agg(DISTINCT ext.extension) AS extensions,
              jsonb_agg(DISTINCT o.expedition) AS other_expeditions
            FROM antarctica.expeditions e
            JOIN antarctica.cruise_lines c ON c.cruise_line_id = e.cruise_line_id
            LEFT JOIN antarctica.gallery g ON g.expedition_id = e.expedition_id
            LEFT JOIN expeditions o ON o.cruise_line_id = e.cruise_line_id
            LEFT JOIN extensions ext ON ext.expedition_id = e.expedition_id
            LEFT JOIN itineraries i ON i.expedition_id = e.expedition_id
            LEFT JOIN departures d ON d.itinerary_id = i.itinerary_id
            LEFT JOIN vessels v ON v.vessel_id = d.vessel_id
            WHERE e.expedition_id = :p_expedition_id
            GROUP BY e.expedition_id, c.cruise_line_id
          """,
      nativeQuery = true)
  Map<String, Object> getById(@Param("p_expedition_id") int id);

  @Query(
      value =
          """
            WITH combined_table AS (
              SELECT
                e.expedition_id as id,
                c.name as cruise_line,
                c.logo as logo,
                min(v.capacity) as capacity,
                e.name,
                e.duration,
                min(d.start_date) as nearest_date,
                e.starting_price,
                e.photo_url
              FROM antarctica.expeditions e
              JOIN antarctica.cruise_lines c ON c.cruise_line_id = e.cruise_line_id
              LEFT JOIN (
                SELECT *
                FROM antarctica.departures d
                WHERE
                  (:start_date IS NULL OR d.start_date >= CAST(:start_date AS DATE)) AND
                  (:end_date IS NULL OR d.end_date <= CAST(:end_date AS DATE))
              ) d ON e.expedition_id = d.expedition_id
              LEFT JOIN antarctica.vessels v ON v.vessel_id = d.vessel_id
              WHERE
                (cardinality(:cruise_lines) = 0 OR c.name = ANY(:cruise_lines)) AND
                (v.capacity BETWEEN :min_capacity AND :max_capacity) AND
                (
                  CASE
                    WHEN POSITION('-' IN e.duration) > 0 THEN
                      CAST(SPLIT_PART(e.duration, '-', 1) AS INTEGER) BETWEEN :min_duration AND :max_duration
                      OR CAST(SPLIT_PART(e.duration, '-', 2) AS INTEGER) BETWEEN :min_duration AND :max_duration
                    ELSE CAST(e.duration AS INTEGER) BETWEEN :min_duration AND :max_duration
                  END
                )
              GROUP BY e.expedition_id, c.name, c.logo
            )
            SELECT * FROM combined_table e
          """,
      countQuery =
          """
          SELECT count(*)
          FROM antarctica.expeditions e
          JOIN antarctica.cruise_lines c ON c.cruise_line_id = e.cruise_line_id
          LEFT JOIN (
            SELECT *
            FROM antarctica.departures d
            WHERE
              (:start_date IS NULL OR d.start_date >= CAST(:start_date AS DATE)) AND
              (:end_date IS NULL OR d.end_date <= CAST(:end_date AS DATE))
          ) d ON e.expedition_id = d.expedition_id
          LEFT JOIN antarctica.vessels v ON v.vessel_id = d.vessel_id
          WHERE
            (cardinality(:cruise_lines) = 0 OR c.name = ANY(:cruise_lines)) AND
            (v.capacity BETWEEN :min_capacity AND :max_capacity) AND
            (
              CASE
                WHEN POSITION('-' IN e.duration) > 0 THEN
                  CAST(SPLIT_PART(e.duration, '-', 1) AS INTEGER) BETWEEN :min_duration AND :max_duration
                  OR CAST(SPLIT_PART(e.duration, '-', 2) AS INTEGER) BETWEEN :min_duration AND :max_duration
                ELSE CAST(e.duration AS INTEGER) BETWEEN :min_duration AND :max_duration
              END
            )
          GROUP BY e.expedition_id, c.name, c.logo
          """,
      nativeQuery = true,
      queryRewriter = ExpeditionQueryWriter.class)
  Page<Map<String, Object>> findAllExpeditionDTO(
      Pageable pageable,
      @Param("start_date") String startDate,
      @Param("end_date") String endDate,
      @Param("cruise_lines") String[] cruiseLines,
      @Param("min_capacity") Integer minCapacity,
      @Param("max_capacity") Integer maxCapacity,
      @Param("min_duration") Integer minDuration,
      @Param("max_duration") Integer maxDuration);

  List<Expedition> findAllByCruiseLine(CruiseLine cruiseLine);

  Optional<Expedition> findByCruiseLineAndName(CruiseLine cruiseLine, String name);
}
