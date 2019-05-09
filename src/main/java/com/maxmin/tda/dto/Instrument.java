package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Instrument {
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("cusip")
    private String cusip;
    @JsonProperty("assetType")
    private String assetType;
    @JsonProperty("description")
    private String description;
}
