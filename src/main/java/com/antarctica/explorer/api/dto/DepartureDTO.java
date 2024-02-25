package com.antarctica.explorer.api.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Map;

public record DepartureDTO(
    int id,
    String name,
    String itinerary,
    String vessel,
    String departingFrom,
    String arrivingAt,
    String duration,
    Date startDate,
    Date endDate,
    BigDecimal startingPrice,
    BigDecimal discountedPrice,
    String website) {

  public DepartureDTO(Map<String, Object> resultMap) {
    this(
        (Integer) resultMap.get("id"),
        (String) resultMap.get("name"),
        (String) resultMap.get("itinerary"),
        (String) resultMap.get("vessel"),
        (String) resultMap.get("departing_from"),
        (String) resultMap.get("arriving_at"),
        (String) resultMap.get("duration"),
        (Date) resultMap.get("start_date"),
        (Date) resultMap.get("end_date"),
        (BigDecimal) resultMap.get("starting_price"),
        (BigDecimal) resultMap.get("discounted_price"),
        (String) resultMap.get("website"));
  }
}
