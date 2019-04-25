package com.maxmin.tda.controllers;

import com.maxmin.tda.clients.TdaClient;
import com.maxmin.tda.dto.Config;
import com.maxmin.tda.dto.Quote;
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
import java.util.List;
import java.util.Map;

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
    public ModelAndView getCode(@RequestParam("code") String code) throws IOException {
        ResponseEntity<String> response = null;
        System.out.println("Authorization Code------" + code);

        tdaClient.getTokeByCode(code);

        System.out.println("Access Token Response ---------" + tdaClient.getToken().getAccess_token());
        List<Quote> list = tdaClient.getQuotes("BA,PG");

        ModelAndView model = new ModelAndView("content");
        model.addObject("list", list);
        model.addObject("symbols", "BA,PG");
        return model;
    }

    @RequestMapping(value = "getSymbols", method = RequestMethod.POST)
    public RedirectView getSymbols(HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) throws IOException {
        String symbols = request.getParameter("symbols");
        String seconds=request.getParameter("seconds");
        List<Quote> list = tdaClient.getQuotes(symbols);
        RedirectView redirectView = new RedirectView();

        redirectAttributes.addFlashAttribute("list", list);
        redirectAttributes.addFlashAttribute("symbols", symbols);
        redirectAttributes.addFlashAttribute("seconds", seconds);
        redirectView.setContextRelative(true);
        redirectView.setUrl("/showSymbols");
        return redirectView;
    }

    @RequestMapping(value = "getSymbols1")
    public List<Quote> getSymbols(HttpServletRequest request) throws IOException {
        String symbols = request.getParameter("symbols");
        return tdaClient.getQuotes(symbols);
    }

    @RequestMapping(value = "showSymbols")
    public ModelAndView showSymbols(HttpServletRequest request) {
        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);
        List<Quote> list = (List<Quote>) flashMap.get("list");
        String symbols = (String) flashMap.get("symbols");
        String seconds = (String) flashMap.get("seconds");
        ModelAndView model = new ModelAndView("content");
        model.addObject("list", list);
        model.addObject("symbols", symbols);
        model.addObject("seconds", seconds);
        return model;
    }

    @GetMapping(value = "/")
    public ModelAndView defaultPage(ModelMap modelMap) {
        Config config = new Config(tdaClient.getClient_id(), tdaClient.getRedirect_uri());
        modelMap.addAttribute("config", config);
        return new ModelAndView("default");
    }


}