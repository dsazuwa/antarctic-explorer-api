package com.antarctica.explorer.api.dto;

import com.antarctica.explorer.api.model.Expedition;
import java.math.BigDecimal;

public class ExpeditionDTO {
  private final Integer id;
  private final String cruiseLine;
  private final String website;
  private final String name;
  private final String description;
  private final String departingFrom;
  private final String arrivingAt;
  private final String duration;
  private final BigDecimal startingPrice;
  private final String photoUrl;

  public ExpeditionDTO(Expedition expedition) {
    this.id = expedition.getId();
    this.cruiseLine = expedition.getCruiseLine().getName();
    this.website = expedition.getWebsite();
    this.name = expedition.getName();
    this.description = expedition.getDescription();
    this.departingFrom = expedition.getDepartingFrom();
    this.arrivingAt = expedition.getArrivingAt();
    this.duration = expedition.getDuration();
    this.startingPrice = expedition.getStartingPrice();
    this.photoUrl = expedition.getPhotoUrl();
  }

  public Integer getId() {
    return id;
  }

  public String getCruiseLine() {
    return cruiseLine;
  }

  public String getWebsite() {
    return website;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getDepartingFrom() {
    return departingFrom;
  }

  public String getArrivingAt() {
    return arrivingAt;
  }

  public String getDuration() {
    return duration;
  }

  public BigDecimal getStartingPrice() {
    return startingPrice;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }
}
