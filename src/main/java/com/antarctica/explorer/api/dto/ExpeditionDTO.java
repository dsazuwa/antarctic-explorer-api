package com.antarctica.explorer.api.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Map;

public record ExpeditionDTO(
    int id,
    String cruiseLine,
    String logo,
    String name,
    String duration,
    BigDecimal startingPrice,
    Date nearestDate,
    String photoUrl) {
  public ExpeditionDTO(Map<String, Object> resultMap) {
    this(
        (Integer) resultMap.get("id"),
        (String) resultMap.get("cruise_line"),
        (String) resultMap.get("logo"),
        (String) resultMap.get("name"),
        (String) resultMap.get("duration"),
        (BigDecimal) resultMap.get("starting_price"),
        (Date) resultMap.get("nearest_date"),
        (String) resultMap.get("photo_url"));
  }
}
