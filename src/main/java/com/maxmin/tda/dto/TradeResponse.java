package com.maxmin.tda.dto;

import lombok.Data;

@Data
public class TradeResponse {
    private String symbol;
    private int quantity;
    private String status;
    private String message;

    public TradeResponse(String symbol, int quantity, String status, String message) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.status = status;
        this.message = message;
    }
}
