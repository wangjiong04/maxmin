package com.maxmin.tda.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxmin.tda.dto.Token;
import com.maxmin.tda.model.Employee;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
import java.util.Arrays;

@Controller
public class TdaController {

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
        System.out.println("Authorization Ccode------" + code);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("access_type", "offline");
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("client_id", "W12345678@AMER.OAUTHAP");
        body.add("redirect_uri", "http://localhost:5000/app/api/connect");
        HttpEntity entity = new HttpEntity<>(body, headers);

        String access_token_url = "https://api.tdameritrade.com/v1/oauth2/token";

        ResponseEntity<Token> token = restTemplate
                .exchange(access_token_url, HttpMethod.POST, entity, Token.class);

        System.out.println("Access Token Response ---------" + token.getBody());


        String symbolURL = "https://api.tdameritrade.com/v1/marketdata/BA/quotes";
        headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token.getBody().getAccess_token());
        entity = new HttpEntity<>(headers);
        ResponseEntity<String> respponse = restTemplate
                .exchange(symbolURL, HttpMethod.GET, entity, String.class);
        ModelAndView model = new ModelAndView("content");
        model.addObject("symbol", respponse.getBody());
        return model;
    }

    @GetMapping(value = "/")
    public ModelAndView defaultPage() {
        return new ModelAndView("default");
    }

}