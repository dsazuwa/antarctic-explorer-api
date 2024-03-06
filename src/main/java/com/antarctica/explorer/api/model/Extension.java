package com.antarctica.explorer.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Set;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(schema = "antarctica", name = "extensions")
public class Extension {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "extension_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "cruise_line_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private CruiseLine cruiseLine;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT[]")
  private String[] description;

  @Column(name = "starting_price")
  private BigDecimal startingPrice;

  @Column(name = "duration", nullable = false)
  private String duration;

  @Column(name = "photo_url", columnDefinition = "TEXT", nullable = false)
  private String photoUrl;

  @Column(name = "website", columnDefinition = "TEXT")
  private String website;

  @ManyToMany(mappedBy = "extensions")
  private Set<Expedition> expeditions;

  protected Extension() {}

  public Extension(
      CruiseLine cruiseLine,
      String name,
      String[] description,
      BigDecimal startingPrice,
      String duration,
      String photoUrl,
      String website) {
    this.cruiseLine = cruiseLine;
    this.name = name;
    this.description = description;
    this.startingPrice = startingPrice;
    this.duration = duration;
    this.photoUrl = photoUrl;
    this.website = website;
  }

  public Integer getId() {
    return id;
  }

  public CruiseLine getCruiseLine() {
    return cruiseLine;
  }

  public String getName() {
    return name;
  }

  public String[] getDescription() {
    return description;
  }

  public BigDecimal getStartingPrice() {
    return startingPrice;
  }

  public String getDuration() {
    return duration;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public String getWebsite() {
    return website;
  }

  public Set<Expedition> getExpeditions() {
    return expeditions;
  }
}
