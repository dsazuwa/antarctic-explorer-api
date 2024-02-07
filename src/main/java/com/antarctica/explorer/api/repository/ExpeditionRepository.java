package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Expedition;
import com.antarctica.explorer.api.pojo.ExpeditionQueryWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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
              GROUP BY e.expedition_id, c.name
            )
            SELECT * FROM combined_table e
          """,
      countQuery = "SELECT count(*) FROM antarctica.expeditions",
      nativeQuery = true,
      queryRewriter = ExpeditionQueryWriter.class)
  Page<Map<String, Object>> findAllExpeditionDTO(
      Specification<ExpeditionDTO> spec, Pageable pageable);
}
