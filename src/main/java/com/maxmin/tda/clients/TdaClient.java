package com.maxmin.tda.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxmin.tda.dto.Quote;
import com.maxmin.tda.dto.Stock;
import com.maxmin.tda.dto.Token;
import com.maxmin.tda.dto.Transaction;
import com.maxmin.tda.utils.ObjectMapperFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Data
public class TdaClient {
    @Value("${client_id}")
    private String client_id;

    @Value("${redirect_uri}")
    private String redirect_uri;

    @Value("${accountId}")
    private String accountId;

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getInstance();

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

    public void getTokeByCode(String code) {
        getTokenFromTda(true, code);
    }

    public List<Quote> getQuotes() throws IOException {
        Map<String,Integer> stocks = getStocks();
        String symbols = stocks.keySet().stream().collect(Collectors.joining(","));
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
                if (stocks.containsKey(quote.getSymbol())){
                    quote.setQuantity(stocks.get(quote.getSymbol()));
                }
                list.add(quote);
            }
        }
        return list;
    }

    @Cacheable("stocks")
    public Map<String,Integer> getStocks() {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyMap();
        }
        String transactionsUrl = String
                .format("https://api.tdameritrade.com/v1/accounts/%s/transactions?type=TRADE", accountId);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = getEntity(accessToken);
        ResponseEntity<List<Transaction>> response = restTemplate
                .exchange(transactionsUrl, HttpMethod.GET, entity,
                        new ParameterizedTypeReference<List<Transaction>>() {
                        });
        List<Transaction> list = response.getBody();
        Map<String, Integer> stocks = list.stream().collect(
                Collectors.groupingBy(transaction -> transaction.getTransactionItem().getInstrument().getSymbol(),
                        Collectors.summingInt(tran -> tran.getTransactionItem().getAmount())
                ));
        return stocks.entrySet().stream().filter(x->x.getValue()>0).collect(Collectors.toMap(x->x.getKey(), x -> x.getValue()));
        /*
        return stocks.entrySet().stream().filter(x -> x.getValue() > 0).map(e -> new Stock(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
                */
    }

    @Cacheable("stocks")
    public List<Stock> getStocks1() {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return Collections.emptyList();
        }
        String transactionsUrl = String
                .format("https://api.tdameritrade.com/v1/accounts/%s/transactions?type=TRADE", accountId);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = getEntity(accessToken);
        ResponseEntity<List<Transaction>> response = restTemplate
                .exchange(transactionsUrl, HttpMethod.GET, entity,
                        new ParameterizedTypeReference<List<Transaction>>() {
                        });
        List<Transaction> list = response.getBody();
        Map<String, Integer> stocks = list.stream().collect(
                Collectors.groupingBy(transaction -> transaction.getTransactionItem().getInstrument().getSymbol(),
                        Collectors.summingInt(tran -> tran.getTransactionItem().getAmount())
                ));
        return stocks.entrySet().stream().filter(x -> x.getValue() > 0).map(e -> new Stock(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private HttpEntity getEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    private void getTokenByRefreshToken(String refreshToken) {
        getTokenFromTda(false, refreshToken);
    }

    private void getTokenFromTda(boolean byCode, String value) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("access_type", "offline");
        if (byCode) {
            body.add("code", value);
            body.add("grant_type", "authorization_code");
        } else {
            body.add("refresh_token", value);
            body.add("grant_type", "refresh_token");
        }

        body.add("client_id", client_id);
        body.add("redirect_uri", redirect_uri);
        HttpEntity entity = new HttpEntity<>(body, headers);

        String access_token_url = "https://api.tdameritrade.com/v1/oauth2/token";

        ResponseEntity<Token> token = restTemplate
                .exchange(access_token_url, HttpMethod.POST, entity, Token.class);
        this.token = token.getBody();
        this.token.setTokenDate(new Date());
    }

    private String getAccessToken() {
        if (null == token) {
            return "";
        }
        if (!isAccessTokenExpired()) {
            return token.getAccess_token();
        }
        if (!isRefreshTokenExpired()) {
            getTokenByRefreshToken(token.getRefresh_token());
            return token.getAccess_token();
        }
        return "";
    }

    private boolean isAccessTokenExpired() {
        if (null == token) {
            return true;
        }
        return isTokenExpired(token.getExpires_in(), token.getTokenDate());
    }

    private boolean isRefreshTokenExpired() {
        if (null == token) {
            return true;
        }
        return isTokenExpired(token.getRefresh_token_expires_in(), token.getTokenDate());
    }

    private boolean isTokenExpired(int expireSeconds, Date tokenDate) {
        return Instant.now().isAfter(tokenDate.toInstant().plusSeconds(expireSeconds));
    }
}
