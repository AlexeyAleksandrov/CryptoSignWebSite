package com.webapi.application.security;

import com.webapi.application.models.SignUpUserForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

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

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String registration(Model model)
    {
        model.addAttribute("registerForm", new SignUpUserForm());
        return "auth/registration";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String signUp(@ModelAttribute("form") SignUpUserForm signUpUserForm, Model model)
    {
        model.addAttribute("registerForm", signUpUserForm);
        if(signUpUserForm.getUsername().length() < 6 || signUpUserForm.getUsername().length() > 20)
        {
            model.addAttribute("loginError", "Имя пользователя должно быть от 6 до 20 символов!");
            return "auth/registration";
        }

        return "redirect:/";
    }
}
