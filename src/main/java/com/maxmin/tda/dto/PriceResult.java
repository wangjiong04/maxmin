package com.maxmin.tda.dto;

import lombok.Data;

import java.util.List;

@Data
public class PriceResult {
    List<PriceDto> candles;
}
