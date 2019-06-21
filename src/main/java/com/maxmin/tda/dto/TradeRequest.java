package com.maxmin.tda.dto;

import lombok.Data;

@Data
public class TradeRequest {
    private String complexOrderStrategyType;
    private String orderType;
    private String session;
    private String duration;
    private String orderStrategyType;
}
