package com.maxmin.tda.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxmin.tda.dto.Config;
import com.maxmin.tda.dto.Quote;
import com.maxmin.tda.dto.Token;
import com.maxmin.tda.utils.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class TdaController {
    @Value("${client_id}")
    private String client_id;

    @Value("${redirect_uri}")
    private String redirect_uri;

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getInstance();

    @PostMapping(value = "redirectPage")
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = "https://auth.tdameritrade.com/auth?response_type=code&redirect_uri=" + URLEncoder
                .encode(request.getParameter("redirect_uri"),
                        StandardCharsets.UTF_8.displayName()) + "&client_id=" + URLEncoder
                .encode(request.getParameter("client_id"), StandardCharsets.UTF_8.displayName());
        response.sendRedirect(url);
    }

    @RequestMapping(value = "app/api/connect", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView getCode(@RequestParam("code") String code) throws IOException {
        ResponseEntity<String> response = null;
        System.out.println("Authorization Code------" + code);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("access_type", "offline");
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("client_id", client_id);
        body.add("redirect_uri", redirect_uri);
        HttpEntity entity = new HttpEntity<>(body, headers);

        String access_token_url = "https://api.tdameritrade.com/v1/oauth2/token";

        ResponseEntity<Token> token = restTemplate
                .exchange(access_token_url, HttpMethod.POST, entity, Token.class);

        System.out.println("Access Token Response ---------" + token.getBody());
        List<Quote> list = getQuotes(token.getBody().getAccess_token(), "BA,PG");

        ModelAndView model = new ModelAndView("content");
        model.addObject("list", list);
        model.addObject("symbols", "BA,PG");
        model.addObject("token", token.getBody().getAccess_token());
        return model;
    }

    @RequestMapping(value = "getSymbols", method = RequestMethod.POST)
    public ModelAndView getSymbols(HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        String token = request.getParameter("token");
        String symbols = request.getParameter("symbols");
        List<Quote> list = getQuotes(token, symbols);

        ModelAndView model = new ModelAndView("content");
        model.addObject("list", list);
        model.addObject("token", token);
        model.addObject("symbols", symbols);
        return model;
    }

    @GetMapping(value = "/")
    public ModelAndView defaultPage(ModelMap modelMap) {
        Config config = new Config(client_id, redirect_uri);
        modelMap.addAttribute("config", config);
        return new ModelAndView("default");
    }

    private List<Quote> getQuotes(String token, String symbols) throws IOException {
        String symbolURL = "https://api.tdameritrade.com/v1/marketdata/quotes?symbol=" + symbols;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity entity = new HttpEntity<>(headers);
        ResponseEntity<String> respponse = restTemplate
                .exchange(symbolURL, HttpMethod.GET, entity, String.class);
        JsonNode node = OBJECT_MAPPER.readTree(respponse.getBody());
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

}