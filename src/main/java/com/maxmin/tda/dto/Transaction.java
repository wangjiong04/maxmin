package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Transaction {
    @JsonProperty("type")
    public String type;
    @JsonProperty("subAccount")
    public String subAccount;
    @JsonProperty("settlementDate")
    public String settlementDate;
    @JsonProperty("orderId")
    public String orderId;
    @JsonProperty("netAmount")
    public Double netAmount;
    @JsonProperty("transactionDate")
    public String transactionDate;
    @JsonProperty("orderDate")
    public String orderDate;
    @JsonProperty("transactionSubType")
    public String transactionSubType;
    @JsonProperty("transactionId")
    public Long transactionId;
    @JsonProperty("cashBalanceEffectFlag")
    public Boolean cashBalanceEffectFlag;
    @JsonProperty("description")
    public String description;
    @JsonProperty("fees")
    public Fee fees;
    @JsonProperty("transactionItem")
    public TransactionItem transactionItem;

}
