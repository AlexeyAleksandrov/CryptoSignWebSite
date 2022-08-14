package com.webapi.application.controllers;

import com.webapi.application.models.auth.SignUpUserForm;
import com.webapi.application.models.sign.SignTemplateModel;
import com.webapi.application.models.user.User;
import com.webapi.application.repositories.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AuthController
{
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsersRepository usersRepository, PasswordEncoder passwordEncoder)
    {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        boolean error = false;  // флаг ошибки

        if(signUpUserForm.getUsername().length() < 6 || signUpUserForm.getUsername().length() > 20) // проверка на длину логина
        {
            model.addAttribute("loginError", "Имя пользователя должно быть от 6 до 20 символов!");
            error = true;
        }

        if(signUpUserForm.getPassword().length() < 6 || signUpUserForm.getPassword().length() > 20) // проверка на длину пароля
        {
            model.addAttribute("passwordError", "Пароль должен содержать от 6 до 20 символов");
            error = true;
        }

        if(!signUpUserForm.getPassword().equals(signUpUserForm.getConfirm_password()))  // если пароли не совпадают
        {
            model.addAttribute("passwordNotConfirm", true);
            error = true;
        }

        if(error)
        {
            model.addAttribute("registerForm", signUpUserForm);
            return "auth/registration";
        }
        else
        {
            if(usersRepository.existsByUsername(signUpUserForm.getUsername()))  // проверяем, что пользователь уже существует
            {
                model.addAttribute("userExists", true);
                return "auth/registration";
            }
            else
            {
                User user = new User();
                user.setUsername(signUpUserForm.getUsername());
                user.setPassword(passwordEncoder.encode(signUpUserForm.getPassword()));

                SignTemplateModel signTemplateModel = new SignTemplateModel();
                signTemplateModel.setSignOwner("Alexey");
                signTemplateModel.setSignCertificate("1234567890");
                signTemplateModel.setSignDateStart("15.07.2022");
                signTemplateModel.setSignDateEnd("15.07.2023");
                signTemplateModel.setDrawLogo(true);
                signTemplateModel.setInsertType(0);
                signTemplateModel.setCheckTransitionToNewPage(false);
                signTemplateModel.setUser(user);

                if (user.getSignTemplates() == null)
                {
                    ArrayList<SignTemplateModel> templateModels = new ArrayList<>();
                    templateModels.add(signTemplateModel);
                    user.setSignTemplates(templateModels);
                }
                else
                {
                    List<SignTemplateModel> templateModels = user.getSignTemplates();
                    templateModels.add(signTemplateModel);
                    user.setSignTemplates(templateModels);
                }

                usersRepository.save(user);
                return "redirect:/login";
            }
        }
    }
}
