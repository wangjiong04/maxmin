package com.maxmin.tda.dto;

import lombok.Data;

@Data
public class PriceDto {
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Integer volume;
    private Long datetime;
}
