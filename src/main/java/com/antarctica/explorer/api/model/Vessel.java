package com.antarctica.explorer.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(schema = "antarctica", name = "vessels")
public class Vessel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "vessel_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "cruise_line_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private CruiseLine cruiseLine;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "capacity", nullable = false)
  private Integer capacity;

  @Column(name = "website", columnDefinition = "TEXT")
  private String website;

  @Column(name = "photo_url", columnDefinition = "TEXT", nullable = false)
  private String photoUrl;

  protected Vessel() {}

  public Vessel(
      CruiseLine cruiseLine, String name, Integer capacity, String website, String photoUrl) {
    this.cruiseLine = cruiseLine;
    this.name = name;
    this.capacity = capacity;
    this.website = website;
    this.photoUrl = photoUrl;
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

  public Integer getCapacity() {
    return capacity;
  }

  public String getWebsite() {
    return website;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }
}
