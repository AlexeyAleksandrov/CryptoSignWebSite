package com.webapi.application.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController
{
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String getLoginPage()
    {
        return "auth/login.html";
    }
}
