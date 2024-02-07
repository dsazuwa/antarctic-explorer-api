package com.antarctica.explorer.api.dto;

import java.math.BigDecimal;
import java.sql.Date;

public record ExpeditionDTO(
    int id,
    String cruiseLine,
    String website,
    String name,
    String description,
    String departingFrom,
    String arrivingAt,
    String duration,
    BigDecimal startingPrice,
    Date nearestDate,
    String photoUrl) {}
