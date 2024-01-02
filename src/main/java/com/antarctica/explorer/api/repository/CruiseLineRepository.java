package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.CruiseLine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface CruiseLineRepository extends Repository<CruiseLine, Long> {
  List<CruiseLine> findAll();

  Optional<CruiseLine> findById(Long id);

  Optional<CruiseLine> findByName(String name);
}
