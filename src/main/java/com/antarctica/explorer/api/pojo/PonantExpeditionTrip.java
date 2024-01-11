package com.antarctica.explorer.api.pojo;

import java.math.BigDecimal;

public record PonantExpeditionTrip(
    String departingFrom,
    String arrivingAt,
    String startDate,
    String endDate,
    BigDecimal startingPrice,
    String shipName,
    String website) {}
