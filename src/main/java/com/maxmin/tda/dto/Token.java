package com.maxmin.tda.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
public class Token {
    private String access_token;
    private String refresh_token;
    private String token_type;
    private int expires_in;
    private int refresh_token_expires_in;
    private Instant tokenDate;
}
