package com.antarctic.explorer.api.model;

import jakarta.persistence.*;

@Entity
@Table(schema = "antarctic", name = "cruise_lines")
public class CruiseLine {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cruise_line_id")
  private Integer id;

  @Column(name = "name", nullable = false, length = 50, unique = true)
  private String name;

  @Column(name = "website", nullable = false, unique = true)
  private String website;

  @Column(name = "fleet_website", unique = true)
  private String fleetWebsite;

  @Column(name = "expedition_website", nullable = false, unique = true)
  private String expeditionWebsite;

  @Column(name = "logo", nullable = false, unique = true)
  private String logo;

  protected CruiseLine() {}

  public CruiseLine(String name, String website, String expeditionWebsite, String logo) {
    this.name = name;
    this.website = website;
    this.expeditionWebsite = expeditionWebsite;
    this.logo = logo;
  }

  public CruiseLine(
      String name, String website, String fleetWebsite, String expeditionWebsite, String logo) {
    this.name = name;
    this.website = website;
    this.fleetWebsite = fleetWebsite;
    this.expeditionWebsite = expeditionWebsite;
    this.logo = logo;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getWebsite() {
    return website;
  }

  public String getFleetWebsite() {
    return fleetWebsite;
  }

  public String getExpeditionWebsite() {
    return expeditionWebsite;
  }

  public String getLogo() {
    return this.logo;
  }
}
