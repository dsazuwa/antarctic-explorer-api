package com.antarctic.explorer.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(schema = "antarctic", name = "itinerary_details")
public class ItineraryDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "detail_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "itinerary_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Itinerary itinerary;

  @Column(name = "day", nullable = false)
  private String day;

  @Column(name = "header", nullable = false)
  private String header;

  @Column(name = "content", columnDefinition = "TEXT[]")
  private String[] content;

  protected ItineraryDetail() {}

  public ItineraryDetail(Itinerary itinerary, String day, String header, String[] content) {
    this.itinerary = itinerary;
    this.day = day;
    this.header = header;
    this.content = content;
  }

  public Integer getId() {
    return id;
  }

  public Itinerary getItinerary() {
    return itinerary;
  }

  public String getDay() {
    return day;
  }

  public String getHeader() {
    return header;
  }

  public String[] getContent() {
    return content;
  }
}
