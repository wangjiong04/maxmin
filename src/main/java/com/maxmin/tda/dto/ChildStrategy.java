package com.maxmin.tda.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChildStrategy {
    private String orderType = "LIMIT";
    private String session = "NORMAL";
    private String duration = "DAY";
    private String orderStrategyType = "SINGLE";
    private double price;
    private List<OrderLeg> orderLegCollection = new ArrayList<>();
}
