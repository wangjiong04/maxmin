package com.maxmin.tda.controllers;

import com.maxmin.tda.clients.AwsClient;
import com.maxmin.tda.clients.TdaClient;
import com.maxmin.tda.dto.*;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class TdaController {
    @Autowired
    private TdaClient tdaClient;

    @Autowired
    private AwsClient awsClient;

    private static final Logger log = LoggerFactory.getLogger((TdaController.class));

    @PostMapping(value = "redirectPage")
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException, CsvException, ParseException {
        List<String> files = awsClient.getFiles();
        log.info(files.toString());
        String accountId = "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accountId")) {
                    accountId = cookie.getValue();
                }
            }
        }
        log.info("account id: " + accountId);
        boolean redirectToTda = redirectToTda(accountId, files);
        if (redirectToTda) {
            tdaClient.setClient_id(request.getParameter("client_id"));
            tdaClient.setRedirect_uri(request.getParameter("redirect_uri"));
            String url = tdaClient.getCodeUrl();
            response.sendRedirect(url);
        } else {
            response.sendRedirect("/redirectToContent");
        }
    }

    private boolean redirectToTda(String accountId, List<String> files) {
        boolean redirectToTda = true;
        try {
            if (StringUtils.isNotEmpty(accountId) && files.contains(accountId)) {
                List<String[]> csv = awsClient.getCSV(accountId);
                if (null == csv) {
                    log.info("get csv null for account:" + accountId);
                    return true;
                }
                if (csv.isEmpty()) {
                    log.info("csv is empty for account:" + accountId);
                    return true;
                }
                log.info(Arrays.toString(csv.get(0)));
                Token token = tdaClient.csvToToken(csv);
                tdaClient.setToken(token);
                if (!tdaClient.isAccessTokenExpired()) {
                    redirectToTda = false;
                }
                if (tdaClient.isAccessTokenExpired() && !tdaClient.isRefreshTokenExpired()) {
                    try {
                        tdaClient.setToken(tdaClient.getTokenByRefreshToken(tdaClient.getToken()));
                        redirectToTda = false;
                    } catch (Exception ex) {
                        log.error("cannot refresh token: " + tdaClient.getToken().getRefresh_token());
                        return true;
                    }
                }
            }
            return redirectToTda;
        } catch (CsvException | IOException ex) {
            log.error("get token from csv error for account: " + accountId, ex);
            return true;
        }
    }

    @GetMapping(value = "redirectToContent")
    public RedirectView redirect(RedirectAttributes redirectAttributes, @CookieValue("accountId") String accountId) {
        RedirectView redirectView = new RedirectView("/content", true, false);
        redirectAttributes.addFlashAttribute("accountId", accountId);

        return redirectView;
    }


    @RequestMapping(value = "app/api/connect", method = {RequestMethod.GET, RequestMethod.POST})
    public RedirectView getCode(@RequestParam("code") String code, HttpServletResponse response,
                                RedirectAttributes redirectAttributes) throws IOException {

        System.out.println("Authorization Code------" + code);

        tdaClient.setToken(tdaClient.getTokeByCode(code));

        System.out.println("Access Token Response ---------" + tdaClient.getToken().getAccess_token());
        List<Account> accounts = tdaClient.getAccounts();
        System.out.println("Get Account");
        String accountId = accounts.get(0).getSecuritiesAccount().getAccountId();

        List<String> files = awsClient.getFiles();
        try {
            awsClient.writeCSV(tdaClient.tokenToCSV(this.tdaClient.getToken()), accountId);
            if (!files.contains(accountId)) {
                files.add(accountId);
                awsClient.refreshFiles(files);
            }
        } catch (Exception ex) {

        }
        RedirectView redirectView = new RedirectView("/content", true, false);
        redirectAttributes.addFlashAttribute("accountId", accountId);

        //redirectView.setUrl(baseUrl + "/content");

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
    public ModelAndView getContent(HttpServletRequest request, HttpServletResponse response, @ModelAttribute(value = "accountId") String accountId) throws IOException {
        //List<Quote> list = tdaClient.getQuotes();
        if (StringUtils.isNotEmpty(accountId)) {
            Cookie cookie = new Cookie("accountId", accountId);
            cookie.setMaxAge(7 * 24 * 60 * 60 * 100); // expires in 7 days
            response.addCookie(cookie);
            ModelAndView model = new ModelAndView("content");
            //model.addObject("list", list);
            return model;
        } else {
            ModelAndView model = new ModelAndView("default");
            model.addObject("config", new Config(tdaClient.getClient_id(), tdaClient.getRedirect_uri()));
            //model.addObject("list", list);
            return model;
        }

    }

    @GetMapping(value = "discord")
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://discord.com/invite/4cxCYac");
    }

    @RequestMapping(value = "showSymbols")
    public ModelAndView showSymbols(HttpServletRequest request) {
        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);
        List<Quote> list = new ArrayList<>();
        if (null != flashMap.get("list")) {
            list = (List<Quote>) flashMap.get("list");
        }
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
        String txt3 = processSymbols(request.getParameter("txt3"));
        ModelAndView model = new ModelAndView("calculateResult");
        List<CalculateDto> calculateDtos = Arrays.stream(txt2.split(";")).map(CalculateDto::of).collect(Collectors.toList());
        List<String> headers = calculateDtos.stream().map(CalculateDto::getSymbol).collect(Collectors.toList());

        List<Map<String, String>> rows = calculate(headers, calculateDtos, txt1, txt3);

        model.addObject("headers", headers);
        model.addObject("rows", rows);
        return model;
    }

    @RequestMapping(value = "optionChain", method = RequestMethod.GET)
    public ResponseEntity<List<OptionChain>> getOptionChain(HttpServletRequest request) {
        String stock = request.getParameter("symbol");
        if (stock == null) {
            return ResponseEntity.badRequest().build();
        }
        stock = stock.replace(" ", "");
        List<String> symbols = Arrays.asList(stock.split(","));
        if (symbols.isEmpty() || symbols.size() > 10) {
            return ResponseEntity.badRequest().build();
        }
        List<OptionChain> chains = symbols.parallelStream().map(tdaClient::getOptionChain).collect(Collectors.toList());
        return ResponseEntity.ok(chains);
    }

    @RequestMapping(value = "OptionChain", method = RequestMethod.GET)
    public ModelAndView getOptionChain1(HttpServletRequest request) {
        String stock = request.getParameter("symbol");
        OptionChain result = tdaClient.getOptionChain(stock);
        ModelAndView model = new ModelAndView("OptionChain");
        model.addObject("result", result);
        return model;
    }

    private Double formatDouble(double d) {
        return (double) Math.round(d * 100) / 100;
    }

    private List<Map<String, String>> calculate(List<String> headers, List<CalculateDto> calculateDtos, String symbol, String txt3) throws IOException {
        headers.add(0, symbol);
        if (StringUtils.isNotEmpty((txt3))) {
            headers.add(0, txt3);
        }
        Map<String, Quote> stockMap = this.tdaClient.getQuoteBySymbols(String.join(",", headers)).stream().collect(Collectors.toMap(Quote::getSymbol, Function.identity()));
        calculateDtos.forEach(calculateDto -> {
            Quote quote = stockMap.get(calculateDto.getSymbol());
            calculateDto.setResult((quote.getLastPrice() - quote.getClosePrice()) * 100 * Double.parseDouble(calculateDto.getAmount()) / quote.getClosePrice());
            calculateDto.setResultString(formatDouble(calculateDto.getResult()).toString() + "%");
        });

        if (StringUtils.isNotEmpty((txt3))) {
            headers.add(2, "COMBINED");
        } else {
            headers.add(1, "COMBINED");
        }


        CalculateDto calculateDto = new CalculateDto("COMBINED");
        calculateDto.setResult(calculateDtos.stream().mapToDouble(CalculateDto::getResult).sum() * 3);
        double d1 = calculateDto.getResult();
        calculateDto.setResultString(formatDouble(calculateDto.getResult()).toString() + "%");
        calculateDtos.add(0, calculateDto);

        calculateDto = new CalculateDto(symbol);
        Quote quote = stockMap.get(symbol);
        calculateDto.setResult((quote.getLastPrice() - quote.getClosePrice()) * 100 / quote.getClosePrice());
        double d2 = calculateDto.getResult();
        calculateDto.setResultString(formatDouble(calculateDto.getResult()).toString() + "%");
        calculateDtos.add(0, calculateDto);

        if (StringUtils.isNotEmpty(txt3)) {
            calculateDto = new CalculateDto(txt3);
            quote = stockMap.get(symbol);
            calculateDto.setResult((quote.getLastPrice() - quote.getClosePrice()) * 100 / quote.getClosePrice());
            calculateDto.setResultString(formatDouble(calculateDto.getResult()).toString() + "%");
            calculateDtos.add(0, calculateDto);
        }


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
