package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Instrument {
    @JsonProperty("symbol")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String symbol;
    @JsonProperty("cusip")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cusip;
    @JsonProperty("assetType")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String assetType;
    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
}
