package com.antarctica.explorer.api.response;

import java.util.Map;

public record VesselResponse(
    String name, String cruiseLine, int capacity, String photoUrl, String website) {
  public VesselResponse(Map<String, Object> resultMap) {
    this(
        (String) resultMap.get("name"),
        (String) resultMap.get("cruise_line"),
        (Integer) resultMap.get("capacity"),
        (String) resultMap.get("photo_url"),
        (String) resultMap.get("website"));
  }
}
