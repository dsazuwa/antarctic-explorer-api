package com.antarctica.explorer.api.response;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.ArrayList;
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
    List<Vessel> vessels,
    List<Itinerary> itineraries,
    List<Departure> departures,
    Extension[] extensions,
    Expedition[] otherExpeditions) {
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
        mapDepartures((String) resultMap.get("departures")),
        mapExtensions((String) resultMap.get("extensions")),
        mapOtherExpeditions((String) resultMap.get("other_expeditions")));
  }

  public static CruiseLine mapCruiseLine(String json) {
    if (json.isEmpty()) return null;

    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
    return new CruiseLine(
        obj.get("id").getAsInt(), obj.get("name").getAsString(), obj.get("logo").getAsString());
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

  private static List<Vessel> mapVessel(String json) {
    List<Vessel> list = new ArrayList<>();

    if (!json.isEmpty()) {
      JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

      for (int i = 0; i < arr.size(); i++) {
        JsonElement element = arr.get(i);
        if (element.isJsonNull()) continue;

        JsonObject obj = element.getAsJsonObject();
        JsonElement cabin = obj.get("cabins");

        list.add(
            new Vessel(
                obj.get("id").getAsInt(),
                obj.get("name").getAsString(),
                getArray(obj, "description"),
                cabin.isJsonNull() ? null : cabin.getAsInt(),
                obj.get("capacity").getAsInt(),
                obj.get("photo_url").getAsString(),
                obj.get("website").getAsString()));
      }
    }

    return list;
  }

  private static List<Itinerary> mapItinerary(String json) {
    List<Itinerary> list = new ArrayList<>();

    if (!json.isEmpty()) {
      JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

      for (int i = 0; i < arr.size(); i++) {
        JsonObject obj = arr.get(i).getAsJsonObject();

        JsonElement startPort = obj.get("start_port");
        JsonElement endPort = obj.get("end_port");
        JsonElement mapUrl = obj.get("map_url");

        list.add(
            new Itinerary(
                obj.get("id").getAsInt(),
                obj.get("name").getAsString(),
                startPort.isJsonNull() ? null : startPort.getAsString(),
                endPort.isJsonNull() ? null : endPort.getAsString(),
                obj.get("duration").getAsInt(),
                mapUrl.isJsonNull() ? null : mapUrl.getAsString(),
                mapSchedule(obj.get("schedule").getAsJsonArray())));
      }
    }

    return list;
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

      departures.add(
          new Departure(obj.get("start_date").getAsString(), obj.get("end_date").getAsString()));
    }

    return departures;
  }

  private static Extension[] mapExtensions(String json) {
    if (json.isEmpty()) return new Extension[0];

    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    if (arr.size() == 1 && arr.get(0).isJsonNull()) return new Extension[0];

    Extension[] extensions = new Extension[arr.size()];

    for (int i = arr.size() - 1; i >= 0; i--) {
      JsonObject obj = arr.get(i).getAsJsonObject();

      JsonElement startingPrice = obj.get("starting_price");
      JsonElement duration = obj.get("duration");
      JsonElement website = obj.get("website");

      extensions[i] =
          new Extension(
              obj.get("name").getAsString(),
              startingPrice.isJsonNull() ? null : startingPrice.getAsBigDecimal(),
              duration.isJsonNull() ? null : duration.getAsInt(),
              website.isJsonNull() ? null : website.getAsString(),
              obj.get("photo_url").getAsString());
    }

    return extensions;
  }

  private static Expedition[] mapOtherExpeditions(String json) {
    if (json.isEmpty()) return new Expedition[0];

    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    Expedition[] expeditions = new Expedition[arr.size()];

    for (int i = arr.size() - 1; i >= 0; i--) {
      JsonObject obj = arr.get(i).getAsJsonObject();
      JsonElement nearestDate = obj.get("nearest_date");
      JsonElement price = obj.get("starting_price");

      expeditions[i] =
          new Expedition(
              obj.get("id").getAsInt(),
              obj.get("name").getAsString(),
              obj.get("duration").getAsString(),
              nearestDate.isJsonNull() ? null : nearestDate.getAsString(),
              price.isJsonNull() ? null : price.getAsBigDecimal(),
              obj.get("photo_url").getAsString());
    }

    return expeditions;
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
      int id,
      String name,
      String startPort,
      String endPort,
      int duration,
      String mapUrl,
      Schedule[] schedules) {}

  public record CruiseLine(int id, String name, String logo) {}

  public record Gallery(String alt, String url) {}

  public record Vessel(
      int id,
      String name,
      String[] description,
      Integer cabins,
      Integer capacity,
      String photoUrl,
      String website) {}

  public record Departure(String startDate, String endDate) {}

  public record Extension(
      String name, BigDecimal startingPrice, Integer duration, String website, String photoUrl) {}

  public record Expedition(
      int id,
      String name,
      String duration,
      String nearestDate,
      BigDecimal startingPrice,
      String photoUrl) {}
}
