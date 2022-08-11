package com.webapi.application.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;

@Controller
public class MainPageController
{
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String mainPage(Authentication authentication, Model model)
    {
        if (authentication != null)
        {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            model.addAttribute("login", userDetails.getUsername());
        }
        else
        {
            model.addAttribute("notlogin", true);
        }
        return "home/mainpage";
    }
}
