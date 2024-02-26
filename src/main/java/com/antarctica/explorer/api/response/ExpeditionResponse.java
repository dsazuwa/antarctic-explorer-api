package com.antarctica.explorer.api.response;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ExpeditionResponse(
    int id,
    String name,
    String[] description,
    String[] highlights,
    String duration,
    BigDecimal startingPrice,
    String website,
    String photoUrl,
    CruiseLine cruiseLine,
    Gallery[] gallery,
    Map<Integer, Vessel> vessels,
    Map<Integer, Itinerary> itineraries,
    List<Departure> departures) {
  public ExpeditionResponse(Map<String, Object> resultMap) {
    this(
        (Integer) resultMap.get("id"),
        (String) resultMap.get("name"),
        (String[]) resultMap.get("description"),
        (String[]) resultMap.get("highlights"),
        (String) resultMap.get("duration"),
        (BigDecimal) resultMap.get("starting_price"),
        (String) resultMap.get("website"),
        (String) resultMap.get("photo_url"),
        mapCruiseLine((String) resultMap.get("cruise_line")),
        mapToGallery((String) resultMap.get("gallery")),
        mapVessel((String) resultMap.get("vessels")),
        mapItinerary((String) resultMap.get("itineraries")),
        mapDepartures((String) resultMap.get("departures")));
  }

  private static CruiseLine mapCruiseLine(String json) {
    if (json.isEmpty()) return null;

    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
    return new CruiseLine(obj.get("name").getAsString(), obj.get("logo").getAsString());
  }

  private static Gallery[] mapToGallery(String json) {
    if (json.isEmpty()) return new Gallery[0];

    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    Gallery[] gallery = new Gallery[arr.size()];

    for (int i = arr.size() - 1; i >= 0; i--) {
      JsonObject obj = arr.get(i).getAsJsonObject();
      JsonElement alt = obj.get("alt");

      gallery[i] =
          new Gallery(alt.isJsonNull() ? null : alt.getAsString(), obj.get("url").getAsString());
    }

    return gallery;
  }

  private static Map<Integer, Vessel> mapVessel(String json) {
    Map<Integer, Vessel> map = new HashMap<>();

    if (!json.isEmpty()) {
      JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

      for (int i = 0; i < arr.size(); i++) {
        JsonElement element = arr.get(i);
        if (element.isJsonNull()) continue;

        JsonObject obj = element.getAsJsonObject();
        JsonElement cabin = obj.get("cabins");
        int id = obj.get("id").getAsInt();

        Vessel vessel =
            new Vessel(
                obj.get("name").getAsString(),
                getArray(obj, "description"),
                cabin.isJsonNull() ? null : cabin.getAsInt(),
                obj.get("capacity").getAsInt(),
                obj.get("photo_url").getAsString(),
                obj.get("website").getAsString());

        map.put(id, vessel);
      }
    }

    return map;
  }

  private static Map<Integer, Itinerary> mapItinerary(String json) {
    Map<Integer, Itinerary> map = new HashMap<>();

    if (!json.isEmpty()) {
      JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

      for (int i = 0; i < arr.size(); i++) {
        JsonObject obj = arr.get(i).getAsJsonObject();
        int id = obj.get("id").getAsInt();
        JsonElement startPort = obj.get("start_port");
        JsonElement endPort = obj.get("end_port");

        Itinerary itinerary =
            new Itinerary(
                obj.get("name").getAsString(),
                startPort.isJsonNull() ? null : startPort.getAsString(),
                endPort.isJsonNull() ? null : endPort.getAsString(),
                obj.get("duration").getAsInt(),
                obj.get("map_url").getAsString(),
                mapSchedule(obj.get("schedule").getAsJsonArray()));

        map.put(id, itinerary);
      }
    }

    return map;
  }

  private static Schedule[] mapSchedule(JsonArray arr) {
    Schedule[] schedule = new Schedule[arr.size()];

    for (int j = 0; j < arr.size(); j++) {
      JsonObject element = arr.get(j).getAsJsonObject();
      schedule[j] =
          new Schedule(
              element.get("day").getAsString(),
              element.get("header").getAsString(),
              getArray(element, "content"));
    }

    return schedule;
  }

  private static List<Departure> mapDepartures(String json) {
    if (json.isEmpty()) return new ArrayList<>();

    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    List<Departure> departures = new ArrayList<>();

    for (JsonElement element : arr) {
      if (element.isJsonNull()) continue;

      JsonObject obj = element.getAsJsonObject();
      JsonElement name = obj.get("name");
      JsonElement price = obj.get("starting_price");

      departures.add(
          new Departure(
              obj.get("itinerary_id").getAsInt(),
              obj.get("vessel_id").getAsInt(),
              name.isJsonNull() ? null : name.getAsString(),
              obj.get("start_date").getAsString(),
              obj.get("end_date").getAsString(),
              price.isJsonNull() ? null : price.getAsBigDecimal()));
    }

    return departures;
  }

  private static String[] getArray(JsonObject obj, String memberName) {
    JsonArray arr = obj.get(memberName).getAsJsonArray();
    String[] content = new String[arr.size()];

    for (int j = 0; j < arr.size(); j++) {
      JsonElement element = arr.get(j);
      content[j] = element.getAsString();
    }

    return content;
  }

  public record Schedule(String day, String header, String[] content) {}

  public record Itinerary(
      String name,
      String startPort,
      String endPort,
      int duration,
      String mapUrl,
      Schedule[] schedules) {}

  public record CruiseLine(String name, String logo) {}

  public record Gallery(String alt, String url) {}

  public record Vessel(
      String name,
      String[] description,
      Integer cabins,
      Integer capacity,
      String photoUrl,
      String website) {}

  public record Departure(
      int itineraryId,
      int vesselId,
      String name,
      String startDate,
      String endDate,
      BigDecimal startingPrice) {}
}
