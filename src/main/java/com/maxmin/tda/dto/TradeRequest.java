package com.maxmin.tda.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TradeRequest {
    private String complexOrderStrategyType = "NONE";
    private String orderType = "MARKET";
    private String session = "NORMAL";
    private String duration = "DAY";
    private String orderStrategyType = "SINGLE";
    private List<OrderLeg> orderLegCollection = new ArrayList<>();

}
