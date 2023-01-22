package com.webapi.application.controllers;

import com.webapi.application.models.auth.LoginForm;
import com.webapi.application.models.auth.SignUpUserForm;
import com.webapi.application.models.sign.SignTemplateModel;
import com.webapi.application.models.user.User;
import com.webapi.application.repositories.SignTemplatesRepository;
import com.webapi.application.repositories.UsersRepository;
import com.webapi.application.security.SecurityConfiguration;
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

    // это просто для того, чтобы Spring Security перебрасывал авторизацию на кастомный адрес, где происходит авторизация
    @RequestMapping(value = "/authlogin", method = RequestMethod.GET)
    public String authlogin()
    {
        return "redirect:/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(Model model)
    {
        model.addAttribute("form", new LoginForm());
        return "auth/login_new";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@ModelAttribute("form") LoginForm loginForm, Model model, HttpServletRequest request)
    {
        if(loginForm.getLogin().length() == 0)  // если не указан логин
        {
            model.addAttribute("UserNotFound", true);
            return "auth/login_new";
        }
        if(loginForm.getPassword().length() == 0)  // если не указан пароль
        {
            model.addAttribute("UserNotFound", true);
            return "auth/login_new";
        }

        User user = usersRepository.findByUsername(loginForm.getLogin()).orElse(null);
        if(user == null)    // если пользователя с таким именем нет
        {
            model.addAttribute("UserNotFound", true);
            return "auth/login_new";
        }

        try
        {
            request.login(loginForm.getLogin(), loginForm.getPassword());  // выполняем принудительную авторизацию
            return "redirect:/";
        }
        catch (ServletException e)  // если произошла ошибка
        {
            if(e.getMessage().equals("Неверные учетные данные пользователя"))
            {
                model.addAttribute("PasswordError", true);
                return "auth/login_new";
            }
            else
            {
                e.printStackTrace();
                return "auth/login_new";
            }
        }
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

                usersRepository.save(user); // сохраняем пользователя

                try
                {
                    request.login(signUpUserForm.getUsername(), signUpUserForm.getPassword());  // выполняем принудительную авторизацию
                    return "redirect:/";
                }
                catch (ServletException e)  // если произошла ошибка
                {
                    e.printStackTrace();
                    return "redirect:/login";
                }
            }
        }
    }
}
