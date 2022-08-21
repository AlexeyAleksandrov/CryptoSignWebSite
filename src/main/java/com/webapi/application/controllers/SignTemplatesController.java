package com.webapi.application.controllers;

import com.webapi.application.models.sign.SignTemplateModel;
import com.webapi.application.models.user.User;
import com.webapi.application.repositories.SignTemplatesRepository;
import com.webapi.application.repositories.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
@RequestMapping("/templates")
public class SignTemplatesController
{
    private final SignTemplatesRepository templatesRepository;
    private final UsersRepository usersRepository;

    @GetMapping("/create")
    private String createTemplatePage(Model model, Authentication authentication)
    {
        String userName;
        if(authentication == null)
        {
            model.addAttribute("notlogin", true);
            return "sign/templates/create";
        }
        else
        {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();  // получаем информацию о пользователе
            userName = userDetails.getUsername();    // получаем имя пользователя
            User user = usersRepository.findByUsername(userName).orElse(null);
            if(user == null)
            {
                model.addAttribute("UserNotFound", true);
                return "sign/templates/create";
            }
            SignTemplateModel signTemplateModel = new SignTemplateModel();
            signTemplateModel.setUser(user);
            model.addAttribute("login", true);
            model.addAttribute("templateModel", signTemplateModel);
            return "sign/templates/create";
        }
    }

    @PostMapping("/create")
    private String createTemplate(@ModelAttribute("templateModel") SignTemplateModel signTemplateModel, Model model, Authentication authentication)
    {
        String userName;
        if(authentication == null)
        {
            model.addAttribute("notlogin", true);
            return "sign/templates/create";
        }
        else
        {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();  // получаем информацию о пользователе
            userName = userDetails.getUsername();    // получаем имя пользователя
            User user = usersRepository.findByUsername(userName).orElse(null);
            if(user == null)
            {
                model.addAttribute("UserNotFound", true);
                return "sign/templates/create";
            }

            // сохраняем данные
            signTemplateModel.setUser(user);
            user.getSignTemplates().add(signTemplateModel);

            templatesRepository.save(signTemplateModel);
            usersRepository.save(user);

            return "sign/service/create";
        }
    }

    @GetMapping("/chose/{index}")
    private String choseTemplate(@PathVariable("index") int index)
    {
        return null;
    }
}
