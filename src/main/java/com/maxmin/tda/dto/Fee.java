package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Fee {
    @JsonProperty("rFee")
    public Integer rFee;
    @JsonProperty("additionalFee")
    public Integer additionalFee;
    @JsonProperty("cdscFee")
    public Integer cdscFee;
    @JsonProperty("regFee")
    public Integer regFee;
    @JsonProperty("otherCharges")
    public Integer otherCharges;
    @JsonProperty("commission")
    public Integer commission;
    @JsonProperty("optRegFee")
    public Integer optRegFee;
    @JsonProperty("secFee")
    public Integer secFee;
}
