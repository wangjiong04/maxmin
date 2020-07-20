package com.maxmin.tda.controllers;

import com.maxmin.tda.clients.TdaClient;
import com.maxmin.tda.dto.Account;
import com.maxmin.tda.dto.CalculateDto;
import com.maxmin.tda.dto.Config;
import com.maxmin.tda.dto.Order;
import com.maxmin.tda.dto.OrderType;
import com.maxmin.tda.dto.Quote;
import com.maxmin.tda.dto.TradeResponse;
import com.maxmin.tda.dto.TradeType;
import com.maxmin.tda.dto.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class TdaController {
    @Autowired
    private TdaClient tdaClient;

    @PostMapping(value = "redirectPage")
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        tdaClient.setClient_id(request.getParameter("client_id"));
        tdaClient.setRedirect_uri(request.getParameter("redirect_uri"));
        String url = tdaClient.getCodeUrl();
        response.sendRedirect(url);
    }


    @RequestMapping(value = "app/api/connect", method = {RequestMethod.GET, RequestMethod.POST})
    public RedirectView getCode(@RequestParam("code") String code, HttpServletResponse response,
                                RedirectAttributes redirectAttributes) throws IOException {

        System.out.println("Authorization Code------" + code);

        tdaClient.getTokeByCode(code);

        System.out.println("Access Token Response ---------" + tdaClient.getToken().getAccess_token());
        List<Account> accounts = tdaClient.getAccounts();
        String accountId = accounts.get(0).getSecuritiesAccount().getAccountId();
        RedirectView redirectView = new RedirectView();
        redirectAttributes.addFlashAttribute("accountId", accountId);
        redirectView.setContextRelative(true);
        redirectView.setUrl("/content");
        return redirectView;
        //response.sendRedirect("/content");
    }

    @RequestMapping(value = "getSymbols", method = RequestMethod.GET)
    public RedirectView getSymbols(HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) throws IOException {
        List<Quote> list = tdaClient.getQuotes(request.getParameter("accountId"));
        RedirectView redirectView = new RedirectView();
        String selectedSymbol = processSymbols(request.getParameter("selectedSymbol"));
        redirectAttributes.addFlashAttribute("list", list);
        redirectAttributes.addFlashAttribute("selectedSymbol", selectedSymbol);
        redirectView.setContextRelative(true);
        redirectView.setUrl("/showSymbols");
        return redirectView;
    }

    @RequestMapping(value = "content")
    public ModelAndView getContent(HttpServletRequest request) throws IOException {
        //List<Quote> list = tdaClient.getQuotes();
        ModelAndView model = new ModelAndView("content");
        //model.addObject("list", list);
        return model;
    }

    @RequestMapping(value = "showSymbols")
    public ModelAndView showSymbols(HttpServletRequest request) {
        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);
        List<Quote> list = (List<Quote>) flashMap.get("list");
        String selectedSymbol = (String) flashMap.get("selectedSymbol");
        ModelAndView model = new ModelAndView("data");
        model.addObject("list", list);

        model.addObject("selectedSymbol", Arrays.asList(selectedSymbol.split(",")));
        return model;
    }

    @GetMapping(value = "transactions")
    public ModelAndView getTransaction(HttpServletRequest request) {
        String startDate = request.getParameter("startDate");
        ModelAndView model = new ModelAndView("transaction");
        List<Transaction> list = tdaClient.getTransaction(request.getParameter("accountId"));
        model.addObject("transactionList", list);
        return model;
    }

    @GetMapping(value = "orders")
    public ModelAndView getOrders(HttpServletRequest request) {
        ModelAndView model = new ModelAndView("orders");
        List<Order> list = tdaClient.getOrders(request.getParameter("accountId"));
        model.addObject("orderList", list);
        return model;
    }

    @GetMapping(value = "price")
    public ResponseEntity<String> getPrice(HttpServletRequest request, TimeZone zone) {
        String startDate = request.getParameter("startDate");
        String stock = request.getParameter("selectedSymbol");
        return ResponseEntity.ok(tdaClient.getPrice(startDate, stock, zone));
    }

    @GetMapping(value = "/")
    public ModelAndView defaultPage(ModelMap modelMap) {
        Config config = new Config(tdaClient.getClient_id(), tdaClient.getRedirect_uri());
        modelMap.addAttribute("config", config);
        return new ModelAndView("default");
    }

    @PostMapping(value = "/buy")
    public ModelAndView buy(HttpServletRequest request) throws IOException {
        return trade(request, TradeType.BUY);
    }

    @PostMapping(value = "/buyWithAmount")
    public ModelAndView buyWithAmount(HttpServletRequest request) throws IOException {
        return tradeWithAmount(request, TradeType.BUY);
    }

    @PostMapping(value = "/btnBuyLimitOCO")
    public ModelAndView buyWithLimitOCO(HttpServletRequest request) throws IOException {
        return tradeWithOCO(request, TradeType.BUY, OrderType.LIMIT);
    }

    @PostMapping(value = "/btnBuyMarketOCO")
    public ModelAndView buyWithMarketOCO(HttpServletRequest request) throws IOException {
        return tradeWithOCO(request, TradeType.BUY, OrderType.MARKET);
    }

    @PostMapping(value = "/sellWithAmount")
    public ModelAndView sellWithAmount(HttpServletRequest request) throws IOException {
        return tradeWithAmount(request, TradeType.SELL);
    }

    @PostMapping(value = "/sell")
    public ModelAndView sell(HttpServletRequest request) throws IOException {
        return trade(request, TradeType.SELL);
    }

    @PostMapping(value = "/sellAll")
    public ModelAndView sellAll(HttpServletRequest request) throws IOException {
        return tradeAll(request);
    }

    @PostMapping(value = "/calculate")
    public ModelAndView calculate(HttpServletRequest request) throws IOException {
        String txt1 = processSymbols(request.getParameter("txt1"));
        String txt2 = processSymbols(request.getParameter("txt2"));
        ModelAndView model = new ModelAndView("calculateResult");
        List<CalculateDto> calculateDtos = Arrays.stream(txt2.split(";")).map(CalculateDto::of).collect(Collectors.toList());
        List<String> headers = calculateDtos.stream().map(CalculateDto::getSymbol).collect(Collectors.toList());

        List<Map<String, String>> rows = calculate(headers, calculateDtos, txt1);

        model.addObject("headers", headers);
        model.addObject("rows", rows);
        return model;
    }

    private Double formatDouble(double d) {
        return (double) Math.round(d * 100) / 100;
    }

    private List<Map<String, String>> calculate(List<String> headers, List<CalculateDto> calculateDtos, String symbol) throws IOException {
        headers.add(0, symbol);
        Map<String, Quote> stockMap = this.tdaClient.getQuoteBySymbols(String.join(",", headers)).stream().collect(Collectors.toMap(Quote::getSymbol, Function.identity()));
        calculateDtos.forEach(calculateDto -> {
            Quote quote = stockMap.get(calculateDto.getSymbol());
            calculateDto.setResult((quote.getHighPrice() - quote.getClosePrice()) * 100 * Double.parseDouble(calculateDto.getAmount()) / quote.getClosePrice());
            calculateDto.setResultString(formatDouble(calculateDto.getResult()).toString() + "%");
        });

        headers.add(1, "COMBINED");
        CalculateDto calculateDto = new CalculateDto("COMBINED");
        calculateDto.setResult(calculateDtos.stream().mapToDouble(CalculateDto::getResult).sum() * 3);
        double d1 = calculateDto.getResult();
        calculateDto.setResultString(formatDouble(calculateDto.getResult()).toString() + "%");
        calculateDtos.add(0, calculateDto);

        calculateDto = new CalculateDto(symbol);
        Quote quote = stockMap.get(symbol);
        calculateDto.setResult((quote.getHighPrice() - quote.getClosePrice()) * 100 / quote.getClosePrice());
        double d2 = calculateDto.getResult();
        calculateDto.setResultString(formatDouble(calculateDto.getResult()).toString() + "%");
        calculateDtos.add(0, calculateDto);


        headers.add(0, "Difference");
        calculateDto = new CalculateDto("Difference");
        calculateDto.setResult(d2 - d1);
        calculateDtos.add(0, calculateDto);
        calculateDto.setResultString(formatDouble(calculateDto.getResult()).toString() + "%");
        Map<String, String> mapRow = calculateDtos.stream().collect(Collectors.toMap(CalculateDto::getSymbol, CalculateDto::getResultString));

        List<Map<String, String>> rows = new ArrayList<>();
        rows.add(mapRow);
        return rows;
    }

    private ModelAndView trade(HttpServletRequest request, TradeType tradeType) throws IOException {
        String selectedSymbol = processSymbols(request.getParameter("selectedSymbol"));
        String strQuantity = request.getParameter("quantity");
        int quantity = Integer.parseInt(strQuantity);
        List<TradeResponse> result = tdaClient
                .stockTrade(selectedSymbol, quantity, tradeType, request.getParameter("accountId"));
        ModelAndView model = new ModelAndView("traderesult");
        model.addObject("traderesult", result);
        return model;
    }

    private ModelAndView tradeAll(HttpServletRequest request) throws IOException {
        String selectedSymbol = processSymbols(request.getParameter("selectedSymbol"));
        List<TradeResponse> result = tdaClient
                .sellAll(selectedSymbol, request.getParameter("accountId"));
        ModelAndView model = new ModelAndView("traderesult");
        model.addObject("traderesult", result);
        return model;
    }

    private ModelAndView tradeWithAmount(HttpServletRequest request, TradeType tradeType) throws IOException {
        String selectedSymbol = processSymbols(request.getParameter("selectedSymbol"));
        String strAmount = request.getParameter("amount");
        double amount = Integer.parseInt(strAmount);
        List<TradeResponse> result = tdaClient
                .stockTradeWithAmount(selectedSymbol, amount, tradeType, request.getParameter("accountId"));
        ModelAndView model = new ModelAndView("traderesult");
        model.addObject("traderesult", result);
        return model;
    }

    private ModelAndView tradeWithOCO(HttpServletRequest request, TradeType tradeType,
                                      OrderType orderType) throws IOException {
        String selectedSymbol = processSymbols(request.getParameter("selectedSymbol"));
        String strGain = request.getParameter("gain");
        String strLoss = request.getParameter("loss");
        String strQuantity = request.getParameter("quantity");
        double gain = Integer.parseInt(strGain);
        double loss = Integer.parseInt(strLoss);
        int quantity = Integer.parseInt(strQuantity);
        List<TradeResponse> result = tdaClient
                .stockTradeWithOCO(selectedSymbol, quantity, gain, loss, tradeType, request.getParameter("accountId"),
                        orderType);
        ModelAndView model = new ModelAndView("traderesult");
        model.addObject("traderesult", result);
        return model;
    }

    private String processSymbols(String symbols) {
        String result = StringUtils.normalizeSpace(symbols);
        return result.replace(" ", ",");
    }
}