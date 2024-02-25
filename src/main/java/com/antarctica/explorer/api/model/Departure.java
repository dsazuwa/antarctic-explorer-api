package com.antarctica.explorer.api.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.*;

@Entity
@Table(schema = "antarctica", name = "departures")
public class Departure {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "departure_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "expedition_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Expedition expedition;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vessel_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Vessel vessel;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "itinerary_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Itinerary itinerary;

  @Column(name = "name")
  private String name;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "starting_price")
  private BigDecimal startingPrice;

  @Column(name = "discounted_price")
  private BigDecimal discountedPrice;

  @Column(name = "website")
  private String website;

  protected Departure() {}

  public Departure(
      Expedition expedition,
      Vessel vessel,
      Itinerary itinerary,
      String name,
      LocalDate startDate,
      LocalDate endDate,
      BigDecimal startingPrice,
      BigDecimal discountedPrice,
      String website) {
    this.expedition = expedition;
    this.vessel = vessel;
    this.itinerary = itinerary;
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
    this.startingPrice = startingPrice;
    this.discountedPrice = discountedPrice;
    this.website = website;
  }

  public Integer getId() {
    return id;
  }

  public Expedition getExpedition() {
    return expedition;
  }

  public Vessel getVessel() {
    return vessel;
  }

  public Itinerary getItinerary() {
    return itinerary;
  }

  public String getName() {
    return name;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public BigDecimal getStartingPrice() {
    return startingPrice;
  }

  public BigDecimal getDiscountedPrice() {
    return discountedPrice;
  }

  public String getWebsite() {
    return website;
  }
}
