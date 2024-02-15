package com.antarctica.explorer.api.response;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.Map;

public record ExpeditionResponse(
    int id,
    String name,
    String description,
    String[] highlights,
    String departingFrom,
    String arrivingAt,
    String duration,
    BigDecimal startingPrice,
    String website,
    String photoUrl,
    CruiseLine cruiseLine,
    Vessel[] vessels,
    Itinerary[] itinerary,
    Departure[] departures) {
  public ExpeditionResponse(Map<String, Object> resultMap) {
    this(
        (Integer) resultMap.get("id"),
        (String) resultMap.get("name"),
        (String) resultMap.get("description"),
        (String[]) resultMap.get("highlights"),
        (String) resultMap.get("departing_from"),
        (String) resultMap.get("arriving_at"),
        (String) resultMap.get("duration"),
        (BigDecimal) resultMap.get("starting_price"),
        (String) resultMap.get("website"),
        (String) resultMap.get("photo_url"),
        mapCruiseLine((String) resultMap.get("cruise_line")),
        mapVessel((String) resultMap.get("vessels")),
        mapItinerary((String) resultMap.get("itinerary")),
        mapDepartures((String) resultMap.get("departures")));
  }

  private static CruiseLine mapCruiseLine(String json) {
    if (json.isEmpty()) return null;

    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
    return new CruiseLine(obj.get("name").getAsString(), obj.get("logo").getAsString());
  }

  private static Vessel[] mapVessel(String json) {
    if (json.isEmpty()) return new Vessel[0];

    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    Vessel[] vessels = new Vessel[arr.size()];

    for (int i = 0; i < arr.size(); i++) {
      JsonObject vesselObj = arr.get(i).getAsJsonObject();
      vessels[i] = new Vessel(vesselObj.get("id").getAsInt(), vesselObj.get("name").getAsString());
    }

    return vessels;
  }

  private static Itinerary[] mapItinerary(String json) {
    if (json.isEmpty()) return new Itinerary[0];

    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    Itinerary[] itineraries = new Itinerary[arr.size()];

    for (int i = 0; i < arr.size(); i++) {
      JsonObject itineraryObj = arr.get(i).getAsJsonObject();
      itineraries[i] =
          new Itinerary(
              itineraryObj.get("day").getAsString(),
              itineraryObj.get("header").getAsString(),
              itineraryObj.get("content").getAsString());
    }

    return itineraries;
  }

  private static Departure[] mapDepartures(String json) {
    if (json.isEmpty()) return new Departure[0];

    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    Departure[] departures = new Departure[arr.size()];

    for (int i = 0; i < arr.size(); i++) {
      JsonObject departureObj = arr.get(i).getAsJsonObject();
      JsonObject vesselObj = departureObj.get("vessel").getAsJsonObject();

      JsonElement name = departureObj.get("name");
      JsonElement startPort = departureObj.get("departing_from");
      JsonElement endPort = departureObj.get("arriving_at");
      departures[i] =
          new Departure(
              name.isJsonNull() ? null : name.getAsString(),
              startPort.isJsonNull() ? null : startPort.getAsString(),
              endPort.isJsonNull() ? null : endPort.getAsString(),
              departureObj.get("start_date").getAsString(),
              departureObj.get("end_date").getAsString(),
              departureObj.get("starting_price").getAsBigDecimal(),
              new Vessel(vesselObj.get("id").getAsInt(), vesselObj.get("name").getAsString()));
    }

    return departures;
  }

  public record CruiseLine(String name, String logo) {}

  public record Itinerary(String day, String header, String content) {}

  public record Departure(
      String name,
      String departingFrom,
      String arrivingAt,
      String startDate,
      String endDate,
      BigDecimal startingPrice,
      Vessel vessel) {}

  public record Vessel(int id, String name) {}
}
