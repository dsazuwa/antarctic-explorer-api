package com.antarctica.explorer.api.dto;

import com.antarctica.explorer.api.model.Expedition;
import java.math.BigDecimal;

public record ExpeditionDTO(
    Integer id,
    String cruiseLine,
    String website,
    String name,
    String description,
    String departingFrom,
    String arrivingAt,
    String duration,
    BigDecimal startingPrice,
    String photoUrl) {

  public ExpeditionDTO(Expedition expedition) {
    this(
        expedition.getId(),
        expedition.getCruiseLine().getName(),
        expedition.getWebsite(),
        expedition.getName(),
        expedition.getDescription(),
        expedition.getDepartingFrom(),
        expedition.getArrivingAt(),
        expedition.getDuration(),
        expedition.getStartingPrice(),
        expedition.getPhotoUrl());
  }
}
