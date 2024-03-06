package com.antarctica.explorer.api.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
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

  @Column(name = "description", columnDefinition = "TEXT[]")
  private String[] description;

  @Column(name = "highlights", columnDefinition = "TEXT[]")
  private String[] highlights;

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

  @ManyToMany(mappedBy = "expeditions")
  private Set<Extension> extensions;

  protected Expedition() {}

  public Expedition(
      CruiseLine cruiseLine,
      String website,
      String name,
      String[] description,
      String[] highlights,
      String departingFrom,
      String arrivingAt,
      String duration,
      BigDecimal startingPrice,
      String photoUrl) {
    this.cruiseLine = cruiseLine;
    this.website = website;
    this.name = name;
    this.description = description;
    this.highlights = highlights;
    this.departingFrom = departingFrom;
    this.arrivingAt = arrivingAt;
    this.duration = duration;
    this.startingPrice = startingPrice;
    this.photoUrl = photoUrl;
    this.extensions = new HashSet<>();
  }

  public Integer getId() {
    return id;
  }

  public CruiseLine getCruiseLine() {
    return cruiseLine;
  }

  public String getWebsite() {
    return website;
  }

  public String getName() {
    return name;
  }

  public String[] getDescription() {
    return description;
  }

  public String[] getHighlights() {
    return highlights;
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

  public Set<Extension> getExtensions() {
    return extensions;
  }
}
