package com.maxmin.tda.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LimitTradeRequest {
    private String complexOrderStrategyType = "NONE";
    private String orderType = "LIMIT";
    private String session = "NORMAL";
    private String duration = "DAY";
    private String orderStrategyType = "TRIGGER";
    private double price;
    private List<OrderLeg> orderLegCollection = new ArrayList<>();
    private List<OrderStrategy> childOrderStrategies = new ArrayList<>();
}
