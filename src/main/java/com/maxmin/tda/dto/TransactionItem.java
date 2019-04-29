package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TransactionItem {
    @JsonProperty("accountId")
    public Long accountId;
    @JsonProperty("amount")
    public Integer amount;
    @JsonProperty("price")
    public Double price;
    @JsonProperty("cost")
    public Double cost;
    @JsonProperty("instruction")
    public String instruction;
    @JsonProperty("instrument")
    public Instrument instrument;

    public Integer getAmount() {
        if ("SELL".equals(instruction)) {
            return amount * -1;
        } else {
            return amount;
        }
    }
}
