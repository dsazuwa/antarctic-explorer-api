package com.antarctic.explorer.api.service;

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
    this.duration = duration;
    this.capacity = capacity;
  }

  public ExpeditionFilter() {
    this(
        null,
        null,
        null,
        new RangedFilter(Integer.MIN_VALUE, Integer.MAX_VALUE),
        new RangedFilter(null, null));
  }

  public record RangedFilter(Integer min, Integer max) {}
}
