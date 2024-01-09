package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.repository.CruiseLineRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CruiseLineService {
  private final CruiseLineRepository repository;

  public CruiseLineService(CruiseLineRepository repository) {
    this.repository = repository;
  }

  public List<CruiseLine> getAll() {
    return repository.findAll();
  }

  public Optional<CruiseLine> findById(Long id) {
    return repository.findById(id);
  }

  public Optional<CruiseLine> findByName(String name) {
    return repository.findByName(name);
  }
}