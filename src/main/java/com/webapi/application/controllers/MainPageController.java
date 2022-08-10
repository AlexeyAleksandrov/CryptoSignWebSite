package com.webapi.application.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainPageController
{
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String mainPage()
    {
        return "home/mainpage.html";
    }
}
