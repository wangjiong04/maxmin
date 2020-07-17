package com.maxmin.tda.dto;

import lombok.Data;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@Data
public class CalculateDto {
    private String symbol;
    private String amount;
    private String result;

    public CalculateDto(String symbol) {
        this.symbol = symbol;
    }

    public CalculateDto(String symbol, String amount) {
        this.symbol = symbol;
        this.amount = amount;
    }

    public static CalculateDto of(String str) {
        String[] strs = str.split("_");
        if (strs.length == 2) {
            return new CalculateDto(strs[0], strs[1]);
        } else {
            return new CalculateDto(strs[0]);
        }
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap();
        map.put(symbol, amount);
        return map;
    }

    private String formatDouble(Double d) {
        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(d);
    }

    public void calculate(double lastPrice, double currentPrice) {
        if (null == amount) {
            result = formatDouble((currentPrice - lastPrice) * 100 / lastPrice) + "%";
        } else {
            Double d = Double.parseDouble(amount);
            result = formatDouble((currentPrice - lastPrice) * d * 100 / lastPrice) + "%";
        }
    }
}
