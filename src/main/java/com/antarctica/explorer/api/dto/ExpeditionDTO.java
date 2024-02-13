package com.antarctica.explorer.api.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Map;

public record ExpeditionDTO(
    int id,
    String cruiseLine,
    String website,
    String name,
    String description,
    String departingFrom,
    String arrivingAt,
    String duration,
    BigDecimal startingPrice,
    Date nearestDate,
    String photoUrl) {
  public ExpeditionDTO(Map<String, Object> resultMap) {
    this(
        (Integer) resultMap.get("id"),
        (String) resultMap.get("cruise_line"),
        (String) resultMap.get("website"),
        (String) resultMap.get("name"),
        (String) resultMap.get("description"),
        (String) resultMap.get("departing_from"),
        (String) resultMap.get("arriving_at"),
        (String) resultMap.get("duration"),
        (BigDecimal) resultMap.get("starting_price"),
        (Date) resultMap.get("nearest_date"),
        (String) resultMap.get("photo_url"));
  }
}
