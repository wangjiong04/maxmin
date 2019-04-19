package com.maxmin.tda.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.maxmin.tda.utils.Constant;

import java.util.Date;

public class Quote {
    @JsonProperty("assetType")
    public String assetType;
    @JsonProperty("symbol")
    public String symbol;
    @JsonProperty("description")
    public String description;
    @JsonProperty("bidPrice")
    public Double bidPrice;
    @JsonProperty("bidSize")
    public Integer bidSize;
    @JsonProperty("bidId")
    public String bidId;
    @JsonProperty("askPrice")
    public Double askPrice;
    @JsonProperty("askSize")
    public Integer askSize;
    @JsonProperty("askId")
    public String askId;
    @JsonProperty("lastPrice")
    public Double lastPrice;
    @JsonProperty("lastSize")
    public Integer lastSize;
    @JsonProperty("lastId")
    public String lastId;
    @JsonProperty("openPrice")
    public Double openPrice;
    @JsonProperty("highPrice")
    public Double highPrice;
    @JsonProperty("lowPrice")
    public Double lowPrice;
    @JsonProperty("bidTick")
    public String bidTick;
    @JsonProperty("closePrice")
    public Double closePrice;
    @JsonProperty("netChange")
    public Double netChange;
    @JsonProperty("totalVolume")
    public Integer totalVolume;
    @JsonProperty("quoteTimeInLong")
    @JsonFormat(pattern = Constant.DATE_FORMAT)
    public Date quoteTimeInLong;
    @JsonProperty("tradeTimeInLong")
    @JsonFormat(pattern = Constant.DATE_FORMAT)
    public Date tradeTimeInLong;
    @JsonProperty("mark")
    public Double mark;
    @JsonProperty("exchange")
    public String exchange;
    @JsonProperty("exchangeName")
    public String exchangeName;
    @JsonProperty("marginable")
    public Boolean marginable;
    @JsonProperty("shortable")
    public Boolean shortable;
    @JsonProperty("volatility")
    public Double volatility;
    @JsonProperty("digits")
    public Integer digits;
    @JsonProperty("52WkHigh")
    public Double _52WkHigh;
    @JsonProperty("52WkLow")
    public Double _52WkLow;
    @JsonProperty("nAV")
    public Double nAV;
    @JsonProperty("peRatio")
    public Double peRatio;
    @JsonProperty("divAmount")
    public Double divAmount;
    @JsonProperty("divYield")
    public Double divYield;
    @JsonProperty("divDate")
    @JsonFormat(pattern = Constant.DATE_FORMAT)
    public Date divDate;
    @JsonProperty("securityStatus")
    public String securityStatus;
    @JsonProperty("regularMarketLastPrice")
    public Double regularMarketLastPrice;
    @JsonProperty("regularMarketLastSize")
    public Integer regularMarketLastSize;
    @JsonProperty("regularMarketNetChange")
    public Double regularMarketNetChange;
    @JsonProperty("regularMarketTradeTimeInLong")
    @JsonFormat(pattern = Constant.DATE_FORMAT)
    public Date regularMarketTradeTimeInLong;
    @JsonProperty("delayed")
    public Boolean delayed;

}
