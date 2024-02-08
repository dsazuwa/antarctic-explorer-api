package com.antarctica.explorer.api.pojo;

public record ExpeditionFilter(String[] cruiseLines, RangedFilter duration, RangedFilter capacity) {
  public ExpeditionFilter(String[] cruiseLines, RangedFilter duration, RangedFilter capacity) {
    this.cruiseLines = cruiseLines == null ? new String[0] : cruiseLines;
    this.duration = duration == null ? new RangedFilter(Integer.MIN_VALUE, Integer.MAX_VALUE) : duration;
    this.capacity = capacity == null ? new RangedFilter(Integer.MIN_VALUE, Integer.MAX_VALUE) : capacity;
  }

  public record RangedFilter(Integer min, Integer max) {}
}
