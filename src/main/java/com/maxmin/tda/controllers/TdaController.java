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

    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    public ModelAndView getEmployeeInfo() {
        return new ModelAndView("getToken");
    }

    @PostMapping(value = "redirectPage")
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = "https://auth.tdameritrade.com/auth?response_type=code&redirect_uri=" + URLEncoder
                .encode("https://localhost:3001/app/api/connect",
                        StandardCharsets.UTF_8.displayName()) + "&client_id=" + URLEncoder
                .encode("YAKOBUX999911@AMER.OAUTHAP", StandardCharsets.UTF_8.displayName());
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
        body.add("client_id", "YAKOBUX999911@AMER.OAUTHAP");
        body.add("redirect_uri", "https://localhost:3001/app/api/connect");
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
        ModelAndView model = new ModelAndView("showContent");
        model.addObject("symbol", respponse.getBody());
        return model;
    }

    @RequestMapping(value = "/showEmployees", method = RequestMethod.GET)
    public ModelAndView showEmployees(@RequestParam("code") String code) throws JsonProcessingException, IOException {
        ResponseEntity<String> response = null;
        System.out.println("Authorization Ccode------" + code);

        RestTemplate restTemplate = new RestTemplate();

        String credentials = "javainuse:secret";
        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + encodedCredentials);

        HttpEntity<String> request = new HttpEntity<String>(headers);

        String access_token_url = "http://localhost:8080/oauth/token";
        access_token_url += "?code=" + code;
        access_token_url += "&grant_type=authorization_code";
        access_token_url += "&redirect_uri=http://localhost:8090/showEmployees";

        response = restTemplate.exchange(access_token_url, HttpMethod.POST, request, String.class);

        System.out.println("Access Token Response ---------" + response.getBody());

        // Get the Access Token From the recieved JSON response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.getBody());
        String token = node.path("access_token").asText();

        String url = "http://localhost:8080/user/getEmployeesList";

        // Use the access token for authentication
        HttpHeaders headers1 = new HttpHeaders();
        headers1.add("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers1);

        ResponseEntity<Employee[]> employees = restTemplate.exchange(url, HttpMethod.GET, entity, Employee[].class);
        System.out.println(employees);
        Employee[] employeeArray = employees.getBody();

        ModelAndView model = new ModelAndView("showEmployees");
        model.addObject("employees", Arrays.asList(employeeArray));
        return model;
    }
}