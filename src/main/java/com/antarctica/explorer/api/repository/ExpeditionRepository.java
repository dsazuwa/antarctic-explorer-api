package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Expedition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpeditionRepository extends JpaRepository<Expedition, Long> {
  Optional<Expedition> findByCruiseLineAndName(CruiseLine cruiseLine, String name);
}
