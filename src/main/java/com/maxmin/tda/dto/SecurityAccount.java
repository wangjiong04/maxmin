package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SecurityAccount {
    @JsonProperty("type")
    private String type;
    @JsonProperty("accountId")
    private String accountId;
    @JsonProperty("roundTrips")
    private Long roundTrips;
    @JsonProperty("isDayTrader")
    private Boolean isDayTrader;
    @JsonProperty("isClosingOnlyRestricted")
    private Boolean isClosingOnlyRestricted;
    @JsonProperty("positions")
    private List<Position> positions = null;
    @JsonProperty("initialBalances")
    private Balance initialBalances;
    @JsonProperty("currentBalances")
    private Balance currentBalances;
    @JsonProperty("projectedBalances")
    private ProjectedBalance projectedBalances;
}
