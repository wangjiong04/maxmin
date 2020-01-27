package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LimitTradeRequest {
    private String complexOrderStrategyType = "NONE";
    private String orderType = "LIMIT";
    private String session = "NORMAL";
    private String duration = "DAY";
    private String orderStrategyType = "TRIGGER";
    private Double price;
    private List<OrderLeg> orderLegCollection = new ArrayList<>();
    private List<OrderStrategy> childOrderStrategies = new ArrayList<>();
}
