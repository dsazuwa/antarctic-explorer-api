package com.antarctic.explorer.api.repository;

import com.antarctic.explorer.api.model.CruiseLine;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CruiseLineRepository extends JpaRepository<CruiseLine, Integer> {
  Optional<CruiseLine> findByName(String name);
}
