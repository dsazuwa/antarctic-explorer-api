package com.antarctica.explorer.api.response;

import com.antarctica.explorer.api.model.CruiseLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public record CruiseLineDTO(
    Integer id, String name, String website, String logo, List<Expedition> expeditions) {

  public CruiseLineDTO(
      CruiseLine cruiseLine, List<com.antarctica.explorer.api.model.Expedition> expeditions) {
    this(
        cruiseLine.getId(),
        cruiseLine.getName(),
        cruiseLine.getWebsite(),
        cruiseLine.getLogo(),
        expeditions.stream().map(Expedition::new).collect(Collectors.toList()));
  }
}

record Expedition(
    Integer id,
    String website,
    String name,
    String[] description,
    String departingFrom,
    String arrivingAt,
    String duration,
    BigDecimal startingPrice,
    String photoUrl) {
  public Expedition(com.antarctica.explorer.api.model.Expedition expedition) {
    this(
        expedition.getId(),
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
