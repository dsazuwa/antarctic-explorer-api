package com.antarctica.explorer.api.response;

import static com.antarctica.explorer.api.response.ExpeditionResponse.mapCruiseLine;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Map;

public record ExpeditionDTO(
    int id,
    ExpeditionResponse.CruiseLine cruiseLine,
    String name,
    String duration,
    BigDecimal startingPrice,
    Date nearestDate,
    String photoUrl) {
  public ExpeditionDTO(Map<String, Object> resultMap) {
    this(
        (Integer) resultMap.get("id"),
        mapCruiseLine((String) resultMap.get("cruise_line_obj")),
        (String) resultMap.get("name"),
        (String) resultMap.get("duration"),
        (BigDecimal) resultMap.get("starting_price"),
        (Date) resultMap.get("nearest_date"),
        (String) resultMap.get("photo_url"));
  }
}
