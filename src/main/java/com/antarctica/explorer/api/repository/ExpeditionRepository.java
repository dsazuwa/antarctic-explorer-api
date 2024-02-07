package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.dto.ExpeditionDTO;
import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Expedition;
import java.util.List;
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
            SELECT new com.antarctica.explorer.api.dto.ExpeditionDTO(
                e.id,
                e.cruiseLine.name,
                e.website,
                e.name,
                e.description,
                e.departingFrom,
                e.arrivingAt,
                e.duration,
                e.startingPrice,
                MIN(d.startDate),
                e.photoUrl
            )
            FROM Expedition e
            LEFT JOIN Departure d ON d.expedition.id = e.id
            GROUP BY e.id, e.cruiseLine.name
          """,
      countQuery = "SELECT count(*) FROM Expedition")
  Page<ExpeditionDTO> findAllWithNearestDateAndCapacity(
      Specification<ExpeditionDTO> spec, Pageable pageable);

  @Query(
      value =
          """
            SELECT new com.antarctica.explorer.api.dto.ExpeditionDTO(
                e.id,
                e.cruiseLine.name,
                e.website,
                e.name,
                e.description,
                e.departingFrom,
                e.arrivingAt,
                e.duration,
                e.startingPrice,
                MIN(d.startDate),
                e.photoUrl
            )
            FROM Expedition e
            LEFT JOIN Departure d ON d.expedition.id = e.id
            GROUP BY e.id, e.cruiseLine.name
            ORDER BY MIN(d.startDate) ASC
          """,
      countQuery = "SELECT count(*) FROM Expedition")
  Page<ExpeditionDTO> findAllSortedByNearestDate(
      Specification<ExpeditionDTO> spec, Pageable pageable);

}
