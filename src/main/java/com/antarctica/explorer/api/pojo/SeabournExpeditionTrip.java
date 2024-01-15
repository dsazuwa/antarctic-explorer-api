package com.antarctica.explorer.api.pojo;

import java.math.BigDecimal;

public record SeabournExpeditionTrip(
    String startingDate, BigDecimal startingPrice, String shipName, String website) {}
