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

  @Column(name = "name")
  private String name;

  @Column(name = "departing_from")
  private String departingFrom;

  @Column(name = "arriving_at")
  private String arrivingAt;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "starting_price", nullable = false)
  private BigDecimal startingPrice;

  @Column(name = "website")
  private String website;

  protected Departure() {}

  public Departure(
      Expedition expedition,
      String name,
      String departingFrom,
      String arrivingAt,
      LocalDate startDate,
      LocalDate endDate,
      BigDecimal startingPrice,
      String website) {
    this.expedition = expedition;
    this.name = name;
    this.departingFrom = departingFrom;
    this.arrivingAt = arrivingAt;
    this.startDate = startDate;
    this.endDate = endDate;
    this.startingPrice = startingPrice;
    this.website = website;
  }

  public Integer getId() {
    return id;
  }

  public Expedition getExpedition() {
    return expedition;
  }

  public String getName() {
    return name;
  }

  public String getDepartingFrom() {
    return departingFrom;
  }

  public String getArrivingAt() {
    return arrivingAt;
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

  public String getWebsite() {
    return website;
  }
}
