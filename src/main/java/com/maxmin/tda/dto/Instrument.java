package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Instrument {
    @JsonProperty("symbol")
    public String symbol;
    @JsonProperty("cusip")
    public String cusip;
    @JsonProperty("assetType")
    public String assetType;
}
