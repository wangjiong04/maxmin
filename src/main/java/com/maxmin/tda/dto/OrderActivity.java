package com.maxmin.tda.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderActivity {
    private String activityType;
    private String executionType;
    private int quantity;
    private int orderRemainingQuantity;
    private List<ExecutionLeg> executionLegs;
}
