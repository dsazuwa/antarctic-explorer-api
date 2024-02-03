package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.Vessel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VesselRepository extends JpaRepository<Vessel, Integer> {
  Optional<Vessel> findByName(String name);
}
