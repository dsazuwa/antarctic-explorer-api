package com.antarctic.explorer.api.response;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Map;

public record ExpeditionsDTO(
    int id,
    ExpeditionResponse.CruiseLine cruiseLine,
    String name,
    String duration,
    BigDecimal startingPrice,
    Date nearestDate,
    String photoUrl) {
  public ExpeditionsDTO(Map<String, Object> resultMap) {
    this(
        (Integer) resultMap.get("id"),
        ExpeditionResponse.mapCruiseLine((String) resultMap.get("cruise_line_obj")),
        (String) resultMap.get("name"),
        (String) resultMap.get("duration"),
        (BigDecimal) resultMap.get("starting_price"),
        (Date) resultMap.get("nearest_date"),
        (String) resultMap.get("photo_url"));
  }
}
