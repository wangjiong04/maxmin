package com.maxmin.tda.dto;

import lombok.Data;

import java.util.Dictionary;

@Data
public class OptionChain
{
    private String symbol;
    private String status;
    private Underlying underlying;
    private String strategy;
    private double interval;
    private boolean isDelayed;
    private boolean isIndex;
    private double interestRate;
    private double underlyingPrice;
    private double volatility;
    private double daysToExpiration;
    private int numberOfContracts;
    private Dictionary<String, Dictionary<String, ExpDate[]>> putExpDateMap;
    private Dictionary<String, Dictionary<String, ExpDate[]>> callExpDateMap;
}
