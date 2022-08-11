package com.webapi.application.security;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AuthController
{
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(Model model)
    {
        return "auth/login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logoutPage()
    {
        return "redirect:/login";
    }
}
