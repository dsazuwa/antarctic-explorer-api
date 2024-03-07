package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.model.Expedition;
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

  public Optional<Extension> getExtension(CruiseLine cruiseLine, String name) {
    return extensionRepository.findByCruiseLineAndName(cruiseLine, name);
  }

  public Extension saveExtension(
      CruiseLine cruiseLine,
      String name,
      BigDecimal startingPrice,
      int duration,
      String photoUrl,
      String website) {
    Optional<Extension> existingExtension =
        extensionRepository.findByCruiseLineAndName(cruiseLine, name);

    return existingExtension.orElseGet(
        () ->
            extensionRepository.save(
                new Extension(cruiseLine, name, startingPrice, duration, photoUrl, website)));
  }

  public void saveExpeditionExtension(Extension extension, Expedition expedition) {
    extension.addExpedition(expedition);
    extensionRepository.save(extension);
  }

  public void saveExtension(
      CruiseLine cruiseLine,
      Expedition expedition,
      String name,
      BigDecimal startingPrice,
      int duration,
      String photoUrl,
      String website) {
    Optional<Extension> existingExtension =
        extensionRepository.findByCruiseLineAndName(cruiseLine, name);

    Extension extension =
        existingExtension.orElseGet(
            () ->
                extensionRepository.save(
                    new Extension(cruiseLine, name, startingPrice, duration, photoUrl, website)));

    extension.addExpedition(expedition);
    extensionRepository.save(extension);
  }
}
