package com.maxmin.tda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Stock {
    private String symbol;
    private int quantity;


}
