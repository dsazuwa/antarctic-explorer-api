package com.antarctic.explorer.api.repository;

import com.antarctic.explorer.api.model.CruiseLine;
import com.antarctic.explorer.api.model.Vessel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VesselRepository extends JpaRepository<Vessel, Integer> {
  Optional<Vessel> findByName(String name);

  Optional<Vessel> findTopByCruiseLineOrderById(CruiseLine cruiseLine);

  @Query(
      value =
          """
            SELECT
              v.name,
              c.name AS cruise_line,
              v.description,
              v.capacity,
              v.cabins,
              v.photo_url,
              v.website
            FROM antarctic.vessels v
            JOIN antarctic.cruise_lines c ON c.cruise_line_id = v.cruise_line_id
            WHERE v.vessel_id = :vessel_id
          """,
      nativeQuery = true)
  Map<String, Object> getById(@Param("vessel_id") int id);

  @Query(
      value =
          """
            SELECT
              v.name,
              c.name AS cruise_line,
              v.description,
              v.capacity,
              v.cabins,
              v.photo_url,
              v.website
            FROM antarctic.vessels v
            JOIN antarctic.cruise_lines c ON c.cruise_line_id = v.cruise_line_id
          """,
      nativeQuery = true)
  List<Map<String, Object>> getAllVessels();
}
