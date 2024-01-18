package com.antarctica.explorer.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LindbladHit {
  public String productType;
  public String name;
  public List<DepartureDate> departureDates;
  public List<Destination> destinations;
  public int duration;
  public int durationUS;
  public String durationCategory;
  public String durationCategoryUS;
  public List<Ship> ships;
  public boolean featured;
  public String thumbnail;
  public String staticMapUrl;
  public String pageSlug;
  public int priceFromUSD;
  public int priceFromAUD;
  public int originalPriceFromUSD;
  public int originalPriceFromAUD;
  public int discountedPriceFromUSD;
  public int discountedPriceFromAUD;
  public int discount;
  public List<Extension> extensions;
  public String departureDateNext;
  public int nrDepartures;
  public List<Promotion> promotionsUS;
  public List<Promotion> promotionsWorld;
  public List<Promotion> promotionsAU;
  public boolean hasPromotionUS;
  public boolean hasPromotionAU;
  public boolean hasPromotionWorld;
  public ImageAsset imageAsset;
  public String updatedAt;
  public String objectID;
  public Object _highlightResult;

  public LindbladHit() {}

  public static class DepartureDate {
    public String departureId;
    public Long dateFromTimestamp;
    public String dateFromYearMonth;
    public String dateFromYearMonthUS;
  }

  public static class Destination {
    public String name;
    public String pageSlug;
  }

  public static class Ship {
    public String name;
    public String code;
    public String pageSlug;
  }

  public static class Extension {
    public String packageCode;
    public int duration;
    public Long doublePriceFromUSD;
    public Long doublePriceFromAUD;
    public Long singlePriceFromUSD;
    public Long singlePriceFromAUD;
  }

  public static class Promotion {
    public String label;
    public String priceProgram;
    public String key;
    public String type;
  }

  public static class ImageAsset {
    public String transformUrl;
    public int width;
    public int height;
    public String altText;
  }
}
