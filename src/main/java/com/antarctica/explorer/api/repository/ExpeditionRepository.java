package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Expedition;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpeditionRepository
    extends JpaRepository<Expedition, Long>, JpaSpecificationExecutor<Expedition> {
  List<Expedition> findAllByCruiseLine(CruiseLine cruiseLine);

  Optional<Expedition> findByCruiseLineAndName(CruiseLine cruiseLine, String name);

  @Query(
      value =
          """
          WITH combined_table AS (
            SELECT
              e.expedition_id as id,
              c.name as cruise_line,
              min(v.capacity) as capacity,
              e.name,
              e.description,
              e.departing_from,
              e.arriving_at,
              e.duration,
              min(d.start_date) as nearest_date,
              e.starting_price,
              e.website,
              e.photo_url
            FROM antarctica.expeditions e
            JOIN antarctica.cruise_lines c ON c.cruise_line_id = e.cruise_line_id
            LEFT JOIN antarctica.departures d ON e.expedition_id = d.expedition_id
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
            GROUP BY e.expedition_id, c.name
          )
          SELECT * FROM combined_table e
        """,
      countQuery =
          """
          SELECT count(*)
          FROM antarctica.expeditions e
          JOIN antarctica.cruise_lines c ON c.cruise_line_id = e.cruise_line_id
          LEFT JOIN antarctica.departures d ON e.expedition_id = d.expedition_id
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
          GROUP BY e.expedition_id, c.name
        """,
      nativeQuery = true,
      queryRewriter = ExpeditionQueryWriter.class)
  Page<Map<String, Object>> findAllExpeditionDTO(
      Pageable pageable,
      @Param("cruise_lines") String[] cruiseLines,
      @Param("min_capacity") Integer minCapacity,
      @Param("max_capacity") Integer maxCapacity,
      @Param("min_duration") Integer minDuration,
      @Param("max_duration") Integer maxDuration);
}
