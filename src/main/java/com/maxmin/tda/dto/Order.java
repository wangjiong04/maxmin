package com.maxmin.tda.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Order {
    private String session;
    private String duration;
    private String orderType;
    private String complexOrderStrategyType;
    private int quantity;
    private int filledQuantity;
    private int remainingQuantity;
    private String requestedDestination;
    private String destinationLinkName;
    private List<OrderLeg> orderLegCollection;
    private String orderStrategyType;
    private String orderId;
    private boolean cancelable;
    private boolean editable;
    private String status;
    private Date enteredTime;
    private Date closeTime;
    private String tag;
    private int accountId;
}
