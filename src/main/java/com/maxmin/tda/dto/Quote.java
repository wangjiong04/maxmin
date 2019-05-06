package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.maxmin.tda.utils.Constant;
import lombok.Data;

import java.util.Date;

@Data
public class Quote {
    @JsonProperty("assetType")
    private String assetType;
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("description")
    private String description;
    @JsonProperty("bidPrice")
    private Double bidPrice;
    @JsonProperty("bidSize")
    private Integer bidSize;
    @JsonProperty("bidId")
    private String bidId;
    @JsonProperty("askPrice")
    private Double askPrice;
    @JsonProperty("askSize")
    private Integer askSize;
    @JsonProperty("askId")
    private String askId;
    @JsonProperty("lastPrice")
    private Double lastPrice;
    @JsonProperty("lastSize")
    private Integer lastSize;
    @JsonProperty("lastId")
    private String lastId;
    @JsonProperty("openPrice")
    private Double openPrice;
    @JsonProperty("highPrice")
    private Double highPrice;
    @JsonProperty("lowPrice")
    private Double lowPrice;
    @JsonProperty("bidTick")
    private String bidTick;
    @JsonProperty("closePrice")
    private Double closePrice;
    @JsonProperty("netChange")
    private Double netChange;
    @JsonProperty("totalVolume")
    private Integer totalVolume;
    @JsonProperty("quoteTimeInLong")
    @JsonFormat(pattern = Constant.DATE_FORMAT)
    private Date quoteTimeInLong;
    @JsonProperty("tradeTimeInLong")
    @JsonFormat(pattern = Constant.DATE_FORMAT)
    private Date tradeTimeInLong;
    @JsonProperty("mark")
    private Double mark;
    @JsonProperty("exchange")
    private String exchange;
    @JsonProperty("exchangeName")
    private String exchangeName;
    @JsonProperty("marginable")
    private Boolean marginable;
    @JsonProperty("shortable")
    private Boolean shortable;
    @JsonProperty("volatility")
    private Double volatility;
    @JsonProperty("digits")
    private Integer digits;
    @JsonProperty("52WkHigh")
    private Double _52WkHigh;
    @JsonProperty("52WkLow")
    private Double _52WkLow;
    @JsonProperty("nAV")
    private Double nAV;
    @JsonProperty("peRatio")
    private Double peRatio;
    @JsonProperty("divAmount")
    private Double divAmount;
    @JsonProperty("divYield")
    private Double divYield;
    @JsonProperty("divDate")
    @JsonFormat(pattern = Constant.DATE_FORMAT)
    private Date divDate;
    @JsonProperty("securityStatus")
    private String securityStatus;
    @JsonProperty("regularMarketLastPrice")
    private Double regularMarketLastPrice;
    @JsonProperty("regularMarketLastSize")
    private Integer regularMarketLastSize;
    @JsonProperty("regularMarketNetChange")
    private Double regularMarketNetChange;
    @JsonProperty("regularMarketTradeTimeInLong")
    @JsonFormat(pattern = Constant.DATE_FORMAT)
    private Date regularMarketTradeTimeInLong;
    @JsonProperty("delayed")
    private Boolean delayed;

    private int quantity;

    public String getDescription() {
        return description.split(" ")[0];
    }
}
