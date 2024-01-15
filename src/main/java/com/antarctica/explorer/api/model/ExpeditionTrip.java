package com.antarctica.explorer.api.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.sql.Date;
import org.hibernate.annotations.*;

@Entity
@Table(schema = "antarctica", name = "expedition_trips")
public class ExpeditionTrip {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "expedition_trip_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "expedition_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Expedition expedition;

  @Column(name = "departing_from")
  private String departingFrom;

  @Column(name = "arriving_at")
  private String arrivingAt;

  @Column(name = "start_date", nullable = false)
  private Date startDate;

  @Column(name = "end_date", nullable = false)
  private Date endDate;

  @Column(name = "website")
  private String website;

  protected ExpeditionTrip() {}

  public ExpeditionTrip(
      Expedition expedition,
      String description,
      String departingFrom,
      String arrivingAt,
      Date startDate,
      Date endDate,
      String website) {
    this.expedition = expedition;
    this.departingFrom = departingFrom;
    this.arrivingAt = arrivingAt;
    this.startDate = startDate;
    this.endDate = endDate;
    this.website = website;
  }

  public Integer getId() {
    return id;
  }

  public Expedition getExpedition() {
    return expedition;
  }

  public String getDepartingFrom() {
    return departingFrom;
  }

  public String getArrivingAt() {
    return arrivingAt;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public String getWebsite() {
    return website;
  }
}
