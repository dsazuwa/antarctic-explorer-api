package com.antarctica.explorer.api.service;

import java.time.LocalDate;

public record ExpeditionFilter(
    String startDate,
    String endDate,
    String[] cruiseLines,
    RangedFilter duration,
    RangedFilter capacity) {

  public ExpeditionFilter(
      String startDate,
      String endDate,
      String[] cruiseLines,
      RangedFilter duration,
      RangedFilter capacity) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.cruiseLines = cruiseLines == null ? new String[0] : cruiseLines;
    this.duration =
        duration == null ? new RangedFilter(Integer.MIN_VALUE, Integer.MAX_VALUE) : duration;
    this.capacity =
        capacity == null ? new RangedFilter(Integer.MIN_VALUE, Integer.MAX_VALUE) : capacity;
  }

  public ExpeditionFilter() {
    this(null, null, null, null, null);
  }

  public record RangedFilter(Integer min, Integer max) {
    public RangedFilter(Integer min, Integer max) {
      this.min = min == null ? Integer.MIN_VALUE : min;
      this.max = max == null ? Integer.MAX_VALUE : max;
    }
  }
}
