package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Balance {
    @JsonProperty("accruedInterest")
    private Double accruedInterest;
    @JsonProperty("cashAvailableForTrading")
    private Double cashAvailableForTrading;
    @JsonProperty("cashAvailableForWithdrawal")
    private Double cashAvailableForWithdrawal;
    @JsonProperty("cashBalance")
    private Long cashBalance;
    @JsonProperty("bondValue")
    private Long bondValue;
    @JsonProperty("cashReceipts")
    private Long cashReceipts;
    @JsonProperty("liquidationValue")
    private Double liquidationValue;
    @JsonProperty("longOptionMarketValue")
    private Long longOptionMarketValue;
    @JsonProperty("longStockValue")
    private Double longStockValue;
    @JsonProperty("moneyMarketFund")
    private Double moneyMarketFund;
    @JsonProperty("mutualFundValue")
    private Long mutualFundValue;
    @JsonProperty("shortOptionMarketValue")
    private Long shortOptionMarketValue;
    @JsonProperty("shortStockValue")
    private Long shortStockValue;
    @JsonProperty("isInCall")
    private Boolean isInCall;
    @JsonProperty("unsettledCash")
    private Long unsettledCash;
    @JsonProperty("cashDebitCallValue")
    private Long cashDebitCallValue;
    @JsonProperty("pendingDeposits")
    private Long pendingDeposits;
    @JsonProperty("accountValue")
    private Double accountValue;
}
