package com.antarctica.explorer.api.model;

import jakarta.persistence.*;

@Entity
@Table(schema = "antarctica", name = "cruise_lines")
public class CruiseLine {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cruise_line_id")
  private Integer id;

  @Column(name = "name", nullable = false, length = 50, unique = true)
  private String name;

  @Column(name = "website", nullable = false, unique = true)
  private String website;

  @Column(name = "expedition_website", nullable = false, unique = true)
  private String expeditionWebsite;

  protected CruiseLine() {}

  public CruiseLine(String name, String website, String expeditionWebsite) {
    this.name = name;
    this.website = website;
    this.expeditionWebsite = expeditionWebsite;
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

  public String getExpeditionWebsite() {
    return expeditionWebsite;
  }
}
