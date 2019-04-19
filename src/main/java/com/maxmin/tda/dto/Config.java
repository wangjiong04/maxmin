package com.maxmin.tda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Config {
    private String client_id;
    private String redirect_uri;
}
