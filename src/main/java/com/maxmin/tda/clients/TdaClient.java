package com.maxmin.tda.clients;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxmin.tda.dto.*;
import com.maxmin.tda.utils.ObjectMapperFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Data
@Slf4j
public class TdaClient {
    @Value("${client_id}")
    private String client_id;

    @Value("${redirect_uri}")
    private String redirect_uri;

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getInstance()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private Token token;

    private String codeUrl = "https://auth.tdameritrade.com/auth?response_type=code&redirect_uri=%s&client_id=%s";

    public String getCodeUrl() {
        return String.format(codeUrl, getEncodeParameter(redirect_uri), getEncodeParameter(client_id));
    }

    private String getEncodeParameter(String value) {
        try {
            return URLEncoder
                    .encode(value, StandardCharsets.UTF_8.displayName());
        } catch (IOException ex) {
            return value;
        }
    }

    public Token getTokeByCode(String code) {
        return getTokenFromTda(true, code, 0, null);
    }

    public List<Quote> getQuotes(String accountId) throws IOException {
        Map<String, Position> accountStock = getAccountStock(accountId);
        String symbols = accountStock.keySet().stream().collect(Collectors.joining(","));

        //Map<String, Long> stocks = getStocks();

        //String symbols = stocks.keySet().stream().collect(Collectors.joining(","));

        List<Quote> quotes = getQuoteBySymbols(symbols);
        for (Quote quote : quotes
        ) {
            if (accountStock.containsKey(quote.getSymbol())) {
                quote.setQuantity(accountStock.get(quote.getSymbol()).getLongQuantity());
                quote.setAvgPrice(accountStock.get(quote.getSymbol()).getAveragePrice());
            }
        }
        return quotes;
    }

    public List<TradeResponse> stockTradeWithOCO(String symbols, int quantity, double gain, double loss,
                                                 TradeType tradeType, String accountId,
                                                 OrderType orderType) throws IOException {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyList();
        }
        List<Quote> list = getQuoteBySymbols(symbols);
        Map<String, Position> accountStock = getAccountStock(accountId);

        String transactionsUrl = "https://api.tdameritrade.com/v1/accounts/" + accountId + "/orders";
        HttpHeaders headers = getHeader(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        List<TradeResponse> responseList = new ArrayList<>();
        for (Quote quote : list
        ) {
            RestTemplate restTemplate = new RestTemplate();
            LimitTradeRequest tradeRequest = new LimitTradeRequest();
            if (orderType == OrderType.LIMIT) {
                tradeRequest.setPrice(quote.getAskPrice());
            }
            OrderLeg orderLeg = createOrderLeg(accountStock, quote.getSymbol(), tradeType, quantity);
            tradeRequest.getOrderLegCollection().add(orderLeg);
            tradeRequest.setOrderType(orderType.name());
            OrderStrategy orderStrategy = new OrderStrategy();

            ChildStrategy childStrategy1 = new ChildStrategy();
            childStrategy1.setPrice(formatDouble(quote.getAskPrice() * (1 + gain / 100)));
            OrderLeg orderLeg1 = createOrderLeg(accountStock, quote.getSymbol(), TradeType.SELL, quantity);
            childStrategy1.getOrderLegCollection().add(orderLeg1);
            orderStrategy.getChildOrderStrategies().add(childStrategy1);

            ChildStrategy childStrategy2 = new ChildStrategy();
            childStrategy2.setPrice(formatDouble(quote.getAskPrice() * (1 - loss / 100)));
            OrderLeg orderLeg2 = createOrderLeg(accountStock, quote.getSymbol(), TradeType.SELL, quantity);
            childStrategy2.getOrderLegCollection().add(orderLeg2);
            orderStrategy.getChildOrderStrategies().add(childStrategy2);
            tradeRequest.getChildOrderStrategies().add(orderStrategy);
            try {
                HttpEntity entity = new HttpEntity<>(tradeRequest, headers);
                ResponseEntity<String> response = restTemplate
                        .exchange(transactionsUrl, HttpMethod.POST, entity, String.class);
                responseList.add(new TradeResponse(quote.getSymbol(), quantity, response.getStatusCode().name(),
                        response.getBody()));
            } catch (Exception ex) {
                log.error("error for: " + quote.getSymbol(), ex);
            }
            //quote.getHighPrice();
        }
        return responseList;
    }

    public List<TradeResponse> stockTradeWithAmount(String symbols, double amount,
                                                    TradeType tradeType, String accountId) throws IOException {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyList();
        }
        List<Quote> list = getQuoteBySymbols(symbols);
        Map<String, Position> accountStock = getAccountStock(accountId);
        double avgAmount = amount / list.size();
        //List<Account> accounts = getAccounts();
        //String accountId = accounts.get(0).getSecuritiesAccount().getAccountId();
        String transactionsUrl = "https://api.tdameritrade.com/v1/accounts/" + accountId + "/orders";
        HttpHeaders headers = getHeader(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        List<TradeResponse> responseList = new ArrayList<>();
        for (Quote quote : list
        ) {
            int quantity = (int) (avgAmount / quote.getHighPrice());
            RestTemplate restTemplate = new RestTemplate();
            TradeRequest tradeRequest = new TradeRequest();
            OrderLeg orderLeg = createOrderLeg(accountStock, quote.getSymbol(), tradeType, quantity);
            tradeRequest.getOrderLegCollection().add(orderLeg);
            HttpEntity entity = new HttpEntity<>(tradeRequest, headers);
            try {
                ResponseEntity<String> response = restTemplate
                        .exchange(transactionsUrl, HttpMethod.POST, entity, String.class);
                responseList.add(new TradeResponse(quote.getSymbol(), quantity, response.getStatusCode().name(),
                        response.getBody()));
            } catch (Exception ex) {
                log.error("error for: " + quote.getSymbol(), ex);
            }
            //quote.getHighPrice();
        }
        return responseList;
    }

    public List<TradeResponse> stockTrade(String symbols, int quantity, TradeType tradeType,
                                          String accountId) throws IOException {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyList();
        }
        List<Quote> list = getQuoteBySymbols(symbols);
        Map<String, Position> accountStock = getAccountStock(accountId);
        //List<Account> accounts = getAccounts();
        //String accountId = accounts.get(0).getSecuritiesAccount().getAccountId();
        String transactionsUrl = "https://api.tdameritrade.com/v1/accounts/" + accountId + "/orders";
        HttpHeaders headers = getHeader(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        List<TradeResponse> responseList = new ArrayList<>();
        for (Quote quote : list
        ) {
            RestTemplate restTemplate = new RestTemplate();
            TradeRequest tradeRequest = new TradeRequest();
            OrderLeg orderLeg = createOrderLeg(accountStock, quote.getSymbol(), tradeType, quantity);
            tradeRequest.getOrderLegCollection().add(orderLeg);
            HttpEntity entity = new HttpEntity<>(tradeRequest, headers);
            try {
                ResponseEntity<String> response = restTemplate
                        .exchange(transactionsUrl, HttpMethod.POST, entity, String.class);
                responseList.add(new TradeResponse(quote.getSymbol(), quantity, response.getStatusCode().name(),
                        response.getBody()));
            } catch (Exception ex) {
                log.error("error for: " + quote.getSymbol(), ex);
            }
            //quote.getHighPrice();
        }
        return responseList;
    }

    public List<TradeResponse> sellAll(String symbols, String accountId) throws IOException {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyList();
        }
        List<Quote> list = getQuoteBySymbols(symbols);
        Map<String, Position> accountStock = getAccountStock(accountId);

        //List<Account> accounts = getAccounts();
        //String accountId = accounts.get(0).getSecuritiesAccount().getAccountId();
        String transactionsUrl = "https://api.tdameritrade.com/v1/accounts/" + accountId + "/orders";
        HttpHeaders headers = getHeader(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        List<TradeResponse> responseList = new ArrayList<>();
        for (Quote quote : list
        ) {
            if (accountStock.containsKey(quote.getSymbol())) {
                RestTemplate restTemplate = new RestTemplate();
                TradeRequest tradeRequest = new TradeRequest();
                OrderLeg orderLeg = new OrderLeg(quote.getSymbol(), TradeType.SELL.name(),
                        accountStock.get(quote.getSymbol()).getLongQuantity().intValue());
                tradeRequest.getOrderLegCollection().add(orderLeg);
                HttpEntity entity = new HttpEntity<>(tradeRequest, headers);
                try {
                    ResponseEntity<String> response = restTemplate
                            .exchange(transactionsUrl, HttpMethod.POST, entity, String.class);
                    responseList.add(new TradeResponse(quote.getSymbol(),
                            accountStock.get(quote.getSymbol()).getLongQuantity().intValue(),
                            response.getStatusCode().name(),
                            response.getBody()));
                } catch (Exception ex) {
                    log.error("error for: " + quote.getSymbol(), ex);
                }
                //quote.getHighPrice();
            }
        }
        return responseList;
    }

    public List<Transaction> getTransaction(String accountId) {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyList();
        }
        String transactionsUrl = String
                .format("https://api.tdameritrade.com/v1/accounts/%s/transactions?type=TRADE&startDate=%s", accountId,
                        getOneWeekBefore());
        return getListResponse(transactionsUrl, accessToken, new ParameterizedTypeReference<List<Transaction>>() {
        });
    }

    private double formatDouble(double d) {
        return (double) Math.round(d * 100) / 100;
    }

    private OrderLeg createOrderLeg(Map<String, Position> accountStock, String symbol, TradeType tradeType,
                                    int quantity) {
        if (TradeType.SELL == tradeType && !accountStock.containsKey(symbol)) {
            return new OrderLeg(symbol, "SELL_SHORT", quantity);
        } else {
            return new OrderLeg(symbol, tradeType.name(), quantity);
        }
    }

    public List<Order> getOrders(String accountId) {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyList();
        }
        String transactionsUrl = String
                .format("https://api.tdameritrade.com/v1/accounts/%s/orders?fromEnteredTime=%s&toEnteredTime=%s",
                        accountId, getOneWeekBefore(), getCurrentDate());
        return getListResponse(transactionsUrl, accessToken, new ParameterizedTypeReference<List<Order>>() {
        });
    }

    public String getPrice(String date, String stock, TimeZone timeZone) {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return "";
        }
        LocalDateTime localDateTime = LocalDateTime.parse(date);
        Long input = localDateTime.toInstant(timeZone.toZoneId().getRules().getOffset(localDateTime)).toEpochMilli();
        LocalDateTime startDate = localDateTime.minusHours(2);
        LocalDateTime endDate = localDateTime.plusHours(1);
        String start = String
                .valueOf(startDate.toInstant(timeZone.toZoneId().getRules().getOffset(startDate)).toEpochMilli());
        String end = String
                .valueOf(endDate.toInstant(timeZone.toZoneId().getRules().getOffset(endDate)).toEpochMilli());
        String searchUrl = String
                .format("https://api.tdameritrade.com/v1/marketdata/%s/pricehistory?periodType=day&frequencyType=minute&frequency=1&startDate=%s&endDate=%s",
                        stock, start, end);
        try {
            PriceResult response = getResponse(searchUrl, accessToken,
                    new ParameterizedTypeReference<PriceResult>() {
                    });
            List<PriceDto> priceDtos = response.getCandles().stream().filter(e -> e.getDatetime() < input)
                    .collect(Collectors.toList());
            if (priceDtos.isEmpty()) {
                return "Cannot fetch the price.";
            } else {
                return priceDtos.get(priceDtos.size() - 1).getHigh().toString();
            }
        } catch (Exception ex) {
            return "Cannot fetch the price.";
        }
    }

    public List<Account> getAccounts() {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyList();
        }
        String transactionsUrl = "https://api.tdameritrade.com/v1/accounts?fields=positions";
        return getListResponse(transactionsUrl, accessToken, new ParameterizedTypeReference<List<Account>>() {
        });
    }

    private <T> List<T> getListResponse(String transactionsUrl, String accessToken,
                                        ParameterizedTypeReference<List<T>> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = getEntity(accessToken);

        ResponseEntity<List<T>> response = restTemplate
                .exchange(transactionsUrl, HttpMethod.GET, entity, responseType);
        return response.getBody();
    }

    private <T> T getResponse(String transactionsUrl, String accessToken,
                              ParameterizedTypeReference<T> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity entity = getEntity(accessToken);

            ResponseEntity<T> response = restTemplate
                    .exchange(transactionsUrl, HttpMethod.GET, entity, responseType);
            return response.getBody();
        } catch (Exception ex) {
            log.error("Error when trying to get response from tda", ex);
            throw ex;
        }
    }

    private Map<String, Position> getAccountStock(String accountId) {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyMap();
        }
        String transactionsUrl = String
                .format("https://api.tdameritrade.com/v1/accounts/%s?fields=positions", accountId);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = getEntity(accessToken);

        ResponseEntity<Account> response = restTemplate
                .exchange(transactionsUrl, HttpMethod.GET, entity,
                        new ParameterizedTypeReference<Account>() {
                        });
        return response.getBody().getSecuritiesAccount().getPositions().stream()
                .filter(position -> "EQUITY".equals(position.getInstrument().getAssetType()))
                .collect(Collectors.toMap(p -> p.getInstrument().getSymbol(),
                        p -> p));
    }


    public List<Quote> getQuoteBySymbols(String symbols) throws IOException {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyList();
        }
        String symbolURL = "https://api.tdameritrade.com/v1/marketdata/quotes?symbol=" + symbols;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = getEntity(accessToken);
        ResponseEntity<String> response = restTemplate
                .exchange(symbolURL, HttpMethod.GET, entity, String.class);
        JsonNode node = OBJECT_MAPPER.readTree(response.getBody());
        final Iterator<String> fieldNames = node.fieldNames();
        ArrayList<Quote> list = new ArrayList<>();
        while (fieldNames.hasNext()) {
            final String fieldName = fieldNames.next();
            final JsonNode fieldValue = node.get(fieldName);
            if (fieldValue.isObject()) {
                Quote quote = OBJECT_MAPPER.readValue(fieldValue.toString(), Quote.class);

                list.add(quote);
            }
        }
        return list;
    }

    public String getOptionChain(String symbols) {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return "Please log in again.";
        }
        String symbolURL = "https://api.tdameritrade.com/v1/marketdata/chains?symbol=" + symbols;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = getEntity(accessToken);
        ResponseEntity<String> response = restTemplate
                .exchange(symbolURL, HttpMethod.GET, entity, String.class);
        return response.getBody();

    }

    public Token csvToToken(List<String[]> csv) {
        Token token = new Token();
        token.setAccess_token(csv.get(0)[0]);
        token.setRefresh_token(csv.get(0)[1]);
        token.setExpires_in(Integer.valueOf(csv.get(0)[2]));
        token.setRefresh_token_expires_in(Integer.valueOf(csv.get(0)[3]));
        token.setTokenDate(Instant.parse(csv.get(0)[4]));
        token.setToken_type(csv.get(0)[5]);
        token.setRefreshTokenDate(Instant.parse(csv.get(0)[6]));
        return token;
    }

    public List<String[]> tokenToCSV(Token token) {
        String[] strs = new String[7];
        strs[0] = token.getAccess_token();
        strs[1] = token.getRefresh_token();
        strs[2] = String.valueOf(token.getExpires_in());
        strs[3] = String.valueOf(token.getRefresh_token_expires_in());
        strs[4] = token.getTokenDate().toString();
        strs[5] = token.getToken_type();
        strs[6] = token.getRefreshTokenDate().toString();
        List<String[]> list = new ArrayList<>();
        list.add(strs);
        return list;
    }

    private HttpEntity getEntity(String token) {
        return new HttpEntity<>(getHeader(token));
    }

    private HttpHeaders getHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(Date.from(Instant.now()));
    }

    private String getOneWeekBefore() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -7);
        Date d = c.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(d);
    }

    public Token getTokenByRefreshToken(Token currentToken) {
        Token newToken = getTokenFromTda(false, currentToken.getRefresh_token(), currentToken.getRefresh_token_expires_in(), currentToken.getRefreshTokenDate());
        if (null == newToken.getRefresh_token()) {
            newToken.setRefresh_token(currentToken.getRefresh_token());
            newToken.setRefreshTokenDate(currentToken.getRefreshTokenDate());
        }
        return newToken;
    }

    private Token getTokenFromTda(boolean byCode, String value, int expired_in, Instant refreshTokenDate) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();

        if (byCode) {
            body.add("code", value);
            body.add("grant_type", "authorization_code");
            body.add("redirect_uri", redirect_uri);
            body.add("access_type", "offline");
        } else {
            body.add("refresh_token", value);
            body.add("grant_type", "refresh_token");
            if (isRefreshTokenWillExpired(expired_in, refreshTokenDate)) {
                body.add("access_type", "offline");
            }
        }

        body.add("client_id", client_id);

        HttpEntity entity = new HttpEntity<>(body, headers);


        String access_token_url = "https://api.tdameritrade.com/v1/oauth2/token";

        ResponseEntity<Token> token = restTemplate
                .exchange(access_token_url, HttpMethod.POST, entity, Token.class);
        Token token1 = token.getBody();
        token1.setTokenDate(Instant.now());
        token1.setRefreshTokenDate(Instant.now());
        return token1;
    }

    private String getAccessToken() {
        if (null == token) {
            return "";
        }
        if (!isAccessTokenExpired()) {
            return token.getAccess_token();
        }
        if (!isRefreshTokenExpired()) {
            this.token = getTokenByRefreshToken(token);
            return token.getAccess_token();
        }
        return "";
    }

    public boolean isAccessTokenExpired() {

        return null == token || isTokenExpired(token.getExpires_in(), token.getTokenDate());
    }

    public boolean isRefreshTokenExpired() {
        return null == token || isTokenExpired(token.getRefresh_token_expires_in(), token.getRefreshTokenDate());
    }

    public boolean isRefreshTokenWillExpired(int getRefresh_token_expires_in, Instant RefreshTokenDate) {
        return isTokenExpired(getRefresh_token_expires_in - 691200, RefreshTokenDate);
    }

    private boolean isTokenExpired(int expireSeconds, Instant tokenDate) {
        return Instant.now().isAfter(tokenDate.plusSeconds(expireSeconds));
    }
}
