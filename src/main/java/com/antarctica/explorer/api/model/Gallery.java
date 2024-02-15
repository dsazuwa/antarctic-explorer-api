package com.antarctica.explorer.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(schema = "antarctica", name = "gallery")
public class Gallery {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "photo_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "expedition_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Expedition expedition;

  @Column(name = "photo_url", nullable = false)
  private String photoUrl;

  @Column(name = "alt")
  private String alt;

  protected Gallery() {}

  public Gallery(Expedition expedition, String photoUrl, String alt) {
    this.expedition = expedition;
    this.photoUrl = photoUrl;
    this.alt = alt;
  }

  public Integer getId() {
    return id;
  }

  public Expedition getExpedition() {
    return expedition;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public String getAlt() {
    return alt;
  }
}
