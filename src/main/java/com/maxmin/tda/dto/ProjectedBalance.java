package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProjectedBalance {
    @JsonProperty("cashAvailableForTrading")
    private Double cashAvailableForTrading;
    @JsonProperty("cashAvailableForWithdrawal")
    private Double cashAvailableForWithdrawal;
}
