package com.antarctic.explorer.api.response;

import java.util.Map;

public record VesselResponse(
    String name,
    String cruiseLine,
    String[] description,
    int capacity,
    int cabins,
    String photoUrl,
    String website) {
  public VesselResponse(Map<String, Object> resultMap) {
    this(
        (String) resultMap.get("name"),
        (String) resultMap.get("cruise_line"),
        (String[]) resultMap.get("description"),
        (Integer) resultMap.get("capacity"),
        (Integer) resultMap.get("cabins"),
        (String) resultMap.get("photo_url"),
        (String) resultMap.get("website"));
  }
}
