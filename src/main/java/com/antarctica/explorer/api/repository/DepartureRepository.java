package com.antarctica.explorer.api.repository;

import com.antarctica.explorer.api.model.Departure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartureRepository extends JpaRepository<Departure, Long> {}
