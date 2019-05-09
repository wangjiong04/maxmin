package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Position {
    @JsonProperty("shortQuantity")
    private Long shortQuantity;
    @JsonProperty("averagePrice")
    private Double averagePrice;
    @JsonProperty("currentDayProfitLoss")
    private Double currentDayProfitLoss;
    @JsonProperty("currentDayProfitLossPercentage")
    private Double currentDayProfitLossPercentage;
    @JsonProperty("longQuantity")
    private Long longQuantity;
    @JsonProperty("settledLongQuantity")
    private Long settledLongQuantity;
    @JsonProperty("settledShortQuantity")
    private Long settledShortQuantity;
    @JsonProperty("instrument")
    private Instrument instrument;
    @JsonProperty("marketValue")
    private Long marketValue;
}
