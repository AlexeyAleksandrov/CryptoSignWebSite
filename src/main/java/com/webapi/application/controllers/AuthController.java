package com.webapi.application.controllers;

import com.webapi.application.models.auth.SignUpUserForm;
import com.webapi.application.models.sign.SignTemplateModel;
import com.webapi.application.models.user.User;
import com.webapi.application.repositories.SignTemplatesRepository;
import com.webapi.application.repositories.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
public class AuthController
{
    private final UsersRepository usersRepository;  // репозиторий для работы с пользователями
    private final PasswordEncoder passwordEncoder;  // кодировщик паролей
    private final SignTemplatesRepository signTemplatesRepository;  // репозиторий для работы с шаблонами подписей

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(Model model)
    {
        return "auth/login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logoutPage(HttpServletRequest request)
    {
        try
        {
            request.logout();   // выходим из сессии
        }
        catch (ServletException e)
        {
            e.printStackTrace();
        }
        return "redirect:/login";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String registration(Model model)
    {
        model.addAttribute("registerForm", new SignUpUserForm());
        return "auth/registration";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String signUp(@ModelAttribute("form") SignUpUserForm signUpUserForm, Model model, HttpServletRequest request)
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
                model.addAttribute("registerForm", signUpUserForm);
                return "auth/registration";
            }
            else
            {
                User user = new User();
                user.setUsername(signUpUserForm.getUsername());
                user.setPassword(passwordEncoder.encode(signUpUserForm.getPassword()));

//                SignTemplateModel signTemplateModel = new SignTemplateModel();
//                signTemplateModel.setTemplateName("Тестовый шаблон");
//                signTemplateModel.setSignOwner("Alexey");
//                signTemplateModel.setSignCertificate("1234567890");
//                signTemplateModel.setSignDateStart("15.07.2022");
//                signTemplateModel.setSignDateEnd("15.07.2023");
//                signTemplateModel.setDrawLogo(true);
//                signTemplateModel.setInsertType(0);
//                signTemplateModel.setCheckTransitionToNewPage(false);
//                signTemplateModel.setUser(user);
//
//                user.getSignTemplates().add(signTemplateModel); // добавляем подпись к пользователю

                usersRepository.save(user); // сохраняем пользователя
//                signTemplatesRepository.save(signTemplateModel);    // сохраняем шаблон

                try
                {
//                    request.logout();
                    request.login(signUpUserForm.getUsername(), signUpUserForm.getPassword());  // выполняем принудительную авторизацию
                    return "redirect:/";
                }
                catch (ServletException e)  // если произошла ошибка
                {
                    e.printStackTrace();
                    return "redirect:/login";
                }

//                return "redirect:/login";
            }
        }
    }
}
