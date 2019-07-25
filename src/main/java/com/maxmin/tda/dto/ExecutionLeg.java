package com.maxmin.tda.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ExecutionLeg {
    private int legId;
    private int quantity;
    private int mismarkedQuantity;
    private double price;
    private Date time;
}
