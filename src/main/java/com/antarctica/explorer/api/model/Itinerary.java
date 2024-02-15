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

  @Column(name = "day", nullable = false)
  private String day;

  @Column(name = "header", nullable = false)
  private String header;

  @Column(name = "content", columnDefinition = "TEXT[]")
  private String[] content;

  protected Itinerary() {}

  public Itinerary(Expedition expedition, String day, String header, String[] content) {
    this.expedition = expedition;
    this.day = day;
    this.header = header;
    this.content = content;
  }

  public Integer getId() {
    return id;
  }

  public Expedition getExpedition() {
    return expedition;
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
