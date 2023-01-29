package com.webapi.application.controllers;

import com.webapi.application.models.sign.SignTemplateModel;
import com.webapi.application.models.user.User;
import com.webapi.application.repositories.SignTemplatesRepository;
import com.webapi.application.repositories.UsersRepository;
import com.webapi.application.security.SecurityUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
            return "sign/templates/create_template";
        }
        else
        {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();  // получаем информацию о пользователе
            userName = userDetails.getUsername();    // получаем имя пользователя
            User user = usersRepository.findByUsername(userName).orElse(null);
            if(user == null)
            {
                model.addAttribute("UserNotFound", true);
                return "sign/templates/create_template";
            }
            SignTemplateModel signTemplateModel = new SignTemplateModel();
            signTemplateModel.setUser(user);
            model.addAttribute("login", user.getUsername());
            model.addAttribute("templateModel", signTemplateModel);
            return "sign/templates/create_template";
        }
    }

    @PostMapping("/create")
    private String createTemplate(@ModelAttribute("templateModel") SignTemplateModel signTemplateModel, Model model, Authentication authentication)
    {
        String userName;
        if(authentication == null)
        {
            model.addAttribute("notlogin", true);
            return "sign/templates/create_template";
        }
        else
        {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();  // получаем информацию о пользователе
            userName = userDetails.getUsername();    // получаем имя пользователя
            User user = usersRepository.findByUsername(userName).orElse(null);
            if(user == null)
            {
                model.addAttribute("UserNotFound", true);
                return "sign/templates/create_template";
            }

            // сохраняем данные
            signTemplateModel.setUser(user);
            user.getSignTemplates().add(signTemplateModel);

            templatesRepository.save(signTemplateModel);
            usersRepository.save(user);

            return "redirect:/sign/create";
        }
    }

    @GetMapping("/edit/{index}")
    private String editPage(@PathVariable("index") int index, Authentication authentication, Model model)
    {
        if(authentication == null)
        {
            return "redirect:/login";
        }
        else
        {
            String userName = ((UserDetails) authentication.getPrincipal()).getUsername();
            User user = usersRepository.findByUsername(userName).orElse(null);
            if(user == null)
            {
                return "redirect:/login";
            }
            else
            {
                List<SignTemplateModel> templatesList = user.getSignTemplates();    // получаем список шаблонов
                if(index < 0 || index >= templatesList.size())  // если недопустимый индекс
                {
                    model.addAttribute("indexOutOfRange", true);    // ставим ошибку
                }
                else
                {
                    model.addAttribute("templateIndex", index);
                    model.addAttribute("template", templatesList.get(index));   // задаём редактируемый шаблон
                }
                model.addAttribute("login", user.getUsername());
                return "sign/templates/edit_template";
            }
        }
    }

    @PostMapping("/edit")
    private String editTemplate(@ModelAttribute("template") SignTemplateModel signTemplateModel, @RequestParam(name = "templateIndex", defaultValue = "-1") int index, Authentication authentication)
    {
        if(authentication == null)
        {
            return "redirect:/login";
        }
        else
        {
            String userName = ((UserDetails) authentication.getPrincipal()).getUsername();
            User user = usersRepository.findByUsername(userName).orElse(null);
            if(user == null)
            {
                return "redirect:/login";
            }
            else
            {
                List<SignTemplateModel> templatesList = user.getSignTemplates();    // получаем список шаблонов
                if(index < 0 || index >= templatesList.size())  // если недопустимый индекс
                {
                    return "redirect:/sign/create";
                }
                else
                {
                    signTemplateModel.setUser(user);    // указываем, какому пользователю принадлежит отредактированная подпись
                    SignTemplateModel userTemplateModel = user.getSignTemplates().get(index);   // получаем текущий шаблон подписи
                    userTemplateModel.setFromModel(signTemplateModel);  // записываем новые данные
                    user.getSignTemplates().set(index, userTemplateModel);  // обновляем данные пользователю

                    templatesRepository.save(userTemplateModel);
                    usersRepository.save(user);

                    return "redirect:/sign/create";
                }
            }
        }
    }

    @GetMapping("/delete")
    private String deletePage(Model model, Authentication authentication)
    {
        if(authentication == null)
        {
            return "redirect:/login";
        }
        else
        {
            String userName = ((UserDetails) authentication.getPrincipal()).getUsername();
            User user = usersRepository.findByUsername(userName).orElse(null);
            if(user == null)
            {
                return "redirect:/login";
            }
            else
            {
                List<SignTemplateModel> templatesList = user.getSignTemplates();    // получаем список шаблонов
                model.addAttribute("signTemplateModels", templatesList);    // отправляем список шаблонов на форму
                model.addAttribute("login", user.getUsername());    // задаём имя пользователя

                return "sign/templates/delete";
            }
        }
    }

    @PostMapping("/delete")
    private String delete(@RequestParam("index") int[] indexes, Authentication authentication)
    {
        if(authentication == null)
        {
            return "redirect:/login";
        }
        else
        {
            String userName = ((UserDetails) authentication.getPrincipal()).getUsername();
            User user = usersRepository.findByUsername(userName).orElse(null);
            if(user == null)
            {
                return "redirect:/login";
            }
            else
            {
                List<SignTemplateModel> templatesList = user.getSignTemplates();    // получаем список шаблонов
//                List<SignTemplateModel> nextTemplatesList = new ArrayList<>();  // список с сертификатами, которые остаются
                List<SignTemplateModel> deleteTemplatesList = new ArrayList<>();  // список с сертификатами, которые будут удалены

                for (int i = 0; i < templatesList.size(); i++)  // перебор всех шаблонов
                {
                    boolean contains = false;   // индекс содержится в списке на удаление
                    // перебор всех выбранных индексов
                    for (int index : indexes)
                    {
                        if (index == i)
                        {
                            contains = true;
                            break;
                        }
                    }

                    if(contains)    // если индекс содержится в списке на удаление
                    {
                        deleteTemplatesList.add(templatesList.get(i));  // добавляем в список на удаление
                    }
//                    else    // если удалять не надо
//                    {
//                        nextTemplatesList.add(templatesList.get(i));    // добавляем в список, который будет сохранён
//                    }
                }

                templatesRepository.deleteAll(deleteTemplatesList);   // удаляем выбранные

                return "redirect:/sign/create";
            }
        }
    }
}
