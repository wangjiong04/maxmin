package com.maxmin.tda.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderStrategy {
    private String orderStrategyType = "OCO";
    private List<ChildStrategy> childOrderStrategies = new ArrayList<>();
}
