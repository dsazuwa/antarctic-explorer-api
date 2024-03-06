package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Extension;
import com.antarctica.explorer.api.repository.ExtensionRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ExtensionService {
  private final ExtensionRepository extensionRepository;

  public ExtensionService(ExtensionRepository extensionRepository) {
    this.extensionRepository = extensionRepository;
  }

  public void saveExtension(
      CruiseLine cruiseLine,
      String name,
      String[] description,
      BigDecimal startingPrice,
      String duration,
      String photoUrl,
      String website) {
    Optional<Extension> existingExtension =
        extensionRepository.findByCruiseLineAndName(cruiseLine, name);

    if (existingExtension.isEmpty())
      extensionRepository.save(
          new Extension(cruiseLine, name, description, startingPrice, duration, photoUrl, website));
  }
}
