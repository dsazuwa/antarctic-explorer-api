package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.CruiseLine;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CruiseLineRepository extends JpaRepository<CruiseLine, Long> {
  Optional<CruiseLine> findByName(String name);
}
