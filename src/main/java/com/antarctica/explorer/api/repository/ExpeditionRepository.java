package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Expedition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpeditionRepository extends JpaRepository<Expedition, Long> {

  Page<Expedition> findAll(Pageable pageable);

  List<Expedition> findAllByCruiseLine(CruiseLine cruiseLine);

  Optional<Expedition> findByCruiseLineAndName(CruiseLine cruiseLine, String name);
}
