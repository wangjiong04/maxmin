package com.maxmin.tda.controllers;

import com.maxmin.tda.clients.TdaClient;
import com.maxmin.tda.dto.Config;
import com.maxmin.tda.dto.Quote;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void getCode(@RequestParam("code") String code, HttpServletResponse response) throws IOException {

        System.out.println("Authorization Code------" + code);

        tdaClient.getTokeByCode(code);

        System.out.println("Access Token Response ---------" + tdaClient.getToken().getAccess_token());
        response.sendRedirect("/content");
    }

    @RequestMapping(value = "getSymbols", method = RequestMethod.GET)
    public RedirectView getSymbols(HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) throws IOException {
        List<Quote> list = tdaClient.getQuotes();
        RedirectView redirectView = new RedirectView();

        redirectAttributes.addFlashAttribute("list", list);
        redirectView.setContextRelative(true);
        redirectView.setUrl("/showSymbols");
        return redirectView;
    }

    @RequestMapping(value = "content")
    public ModelAndView getContent(HttpServletRequest request) throws IOException {
        List<Quote> list = tdaClient.getQuotes();
        ModelAndView model = new ModelAndView("content");
        model.addObject("list", list);
        return model;
    }

    @RequestMapping(value = "showSymbols")
    public ModelAndView showSymbols(HttpServletRequest request) {
        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);
        List<Quote> list = (List<Quote>) flashMap.get("list");
        ModelAndView model = new ModelAndView("data");
        model.addObject("list", list);
        return model;
    }

    @GetMapping(value = "/")
    public ModelAndView defaultPage(ModelMap modelMap) {
        Config config = new Config(tdaClient.getClient_id(), tdaClient.getRedirect_uri());
        modelMap.addAttribute("config", config);
        return new ModelAndView("default");
    }


}