package com.antarctica.explorer.api.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.*;

@Entity
@Table(schema = "antarctica", name = "expeditions")
public class Expedition {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "expedition_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "cruise_line_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private CruiseLine cruiseLine;

  @Column(name = "website")
  private String website;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "departing_from")
  private String departingFrom;

  @Column(name = "arriving_at")
  private String arrivingAt;

  @Column(name = "duration", nullable = false)
  private String duration;

  @Column(name = "starting_price")
  private BigDecimal startingPrice;

  @Column(name = "photo_url", columnDefinition = "TEXT")
  private String photoUrl;

  @CreationTimestamp(source = SourceType.DB)
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp(source = SourceType.DB)
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected Expedition() {}

  public Expedition(
      CruiseLine cruiseLine,
      String website,
      String name,
      String description,
      String departingFrom,
      String arrivingAt,
      String duration,
      BigDecimal startingPrice,
      String photoUrl) {
    this.cruiseLine = cruiseLine;
    this.website = website;
    this.name = name;
    this.description = description;
    this.departingFrom = departingFrom;
    this.arrivingAt = arrivingAt;
    this.duration = duration;
    this.startingPrice = startingPrice;
    this.photoUrl = photoUrl;
  }

  public Integer getId() {
    return id;
  }

  public CruiseLine getCruiseLine() {
    return cruiseLine;
  }

  public void setCruiseLine(CruiseLine cruiseLine) {
    this.cruiseLine = cruiseLine;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public BigDecimal getStartingPrice() {
    return startingPrice;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  @Override
  public String toString() {
    return "Expedition{"
        + "id="
        + id
        + ", cruiseLine="
        + cruiseLine.getName()
        + ", website='"
        + website
        + '\''
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", departingFrom='"
        + departingFrom
        + '\''
        + ", arrivingAt='"
        + arrivingAt
        + '\''
        + ", duration='"
        + duration
        + '\''
        + ", startingPrice="
        + startingPrice
        + ", photoUrl="
        + photoUrl
        + '}';
  }
}
