package com.maxmin.tda.dto;

import lombok.Data;

@Data
public class OrderLeg {
    private String instruction;
    private int quantity;
    private Instrument instrument;

    public OrderLeg(String symbol, String instruction, int quantity) {
        this.quantity = quantity;
        this.instruction = instruction;
        instrument = new Instrument();
        instrument.setSymbol(symbol);
        instrument.setAssetType("EQUITY");
    }
}
