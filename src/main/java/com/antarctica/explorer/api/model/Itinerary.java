package com.antarctica.explorer.api.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(schema = "antarctica", name = "itineraries")
public class Itinerary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "itinerary_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "expedition_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Expedition expedition;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "departing_from")
  private String startPort;

  @Column(name = "arriving_at")
  private String endPort;

  @Column(name = "duration", nullable = false)
  private String duration;

  @Column(name = "map_url", columnDefinition = "TEXT")
  private String mapUrl;

  protected Itinerary() {}

  public Itinerary(
      Expedition expedition,
      String name,
      String startPort,
      String endPort,
      String duration,
      String mapUrl) {
    this.expedition = expedition;
    this.name = name;
    this.startPort = startPort;
    this.endPort = endPort;
    this.duration = duration;
    this.mapUrl = mapUrl;
  }

  public Integer getId() {
    return id;
  }

  public Expedition getExpedition() {
    return expedition;
  }

  public String getName() {
    return this.name;
  }

  public String getStartPort() {
    return startPort;
  }

  public String getEndPort() {
    return endPort;
  }

  public String getDuration() {
    return duration;
  }

  public String getMapUrl() {
    return mapUrl;
  }
}
