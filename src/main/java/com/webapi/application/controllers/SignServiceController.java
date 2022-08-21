package com.webapi.application.controllers;

import com.webapi.application.models.sign.SignTemplateModel;
import com.webapi.application.models.user.User;
import com.webapi.application.repositories.UsersRepository;
import com.webapi.application.services.handlers.Excel.ExcelHandler;
import com.webapi.application.services.handlers.PDF.PDFHandler;
import com.webapi.application.services.handlers.UploadedFileHandler;
import com.webapi.application.services.handlers.Word.WordHandler;
import com.webapi.application.models.sign.CreateSignFormModel;
import com.webapi.application.services.signImage.SignImageCreator;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.util.List;

@Controller
@AllArgsConstructor // внедрение зависимостей через конструктор
public class SignServiceController
{
    public static final String singImagePath = "temp/sign_image.jpg";   // путь сохранения готового изображения подписи
    public static final String signImageLogoPath = "logo/mirea_gerb_52_65.png";  // путь к гербу для изображения

    // внедрение зависимостей через конструктор
    final SignImageCreator signImageCreator;    // сервис создания изображения подписи
    final WordHandler wordHandler;      // обработчик Word документов
    final ExcelHandler excelHandler;    // обработчик Excel документов
    final PDFHandler pdfHandler;    // обработчик PDF документов

    // репозитории
    UsersRepository usersRepository;

    @RequestMapping(value = "/createsign", method = RequestMethod.GET)
    public String index(Model model, Authentication authentication, @RequestParam(name = "index", required = false, defaultValue = "-1") int index)
    {
        CreateSignFormModel createSignFormModel = null;
        if(authentication != null)  // если пользователь авторизован
        {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = usersRepository.findByUsername(userDetails.getUsername()).orElse(null); // получаем пользователя

            if(user != null)    // если пользователь найден
            {
                List<SignTemplateModel> signTemplateModels = user.getSignTemplates();   // получаем список шаблонов

                model.addAttribute("signTemplateModels", signTemplateModels);   // отправляем список шаблонов

                if(index >= 0 && index < signTemplateModels.size()) // проверяем границы индекса
                {
                    createSignFormModel = signTemplateModels.get(index).toCreateSignFormModel();    // применяем выбранный шаблон
                }
            }
        }

        if(createSignFormModel == null)   // если модель не была добавлена
        {
            createSignFormModel = new CreateSignFormModel();
        }

        model.addAttribute("signModel", createSignFormModel);   // передаём данные для заполнения на форму
        return "sign/service/create";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public @ResponseBody
    String provideUploadInfo()
    {
        return "Вы можете загружать файл с использованием того же URL.";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody
    String handleFileUpload(@ModelAttribute("signModel") CreateSignFormModel createSignFormModel)
    {
        if(createSignFormModel == null)
        {
            return "redirect:/createsign";
        }
        createSignFormModel.setFileName(createSignFormModel.getFile().getOriginalFilename());
        String currentDir = System.getProperty("user.dir");
        String fileName = currentDir + "/uploadedfiles/" + createSignFormModel.getFile().getOriginalFilename();   // получаем оригинальное название файла, который был загружен

        // проверка корректности входных данных
        if(createSignFormModel.getInsertType() == -1
                || (createSignFormModel.getInsertType() == 2
                && !(fileName.endsWith(".docx") || fileName.endsWith(".doc") || fileName.endsWith(".rtf"))))
        {
            return "Error! Выбранный тип подписи не подходит для данного типа файлов!";
        }

        // начинаем обработку файла
        if (!createSignFormModel.getFile().isEmpty())
        {
            try
            {
                // проверяем наличие папок для сохранения и вывода
                boolean uploadDirCreated = true;
                boolean outputDirCreated = true;
                boolean tempDirCreated = true;

                File outputDir = new File("output/");   // папка вывода
                File uploadDir = new File("uploadedfiles/");    // папка сохранения
                File tempDir = new File("temp/");   // папка для временных данных

                if(!uploadDir.exists())
                {
                    uploadDirCreated = uploadDir.mkdir();
                }
                if(!outputDir.exists())
                {
                    outputDirCreated = outputDir.mkdir();
                }
                if(!tempDir.exists())
                {
                    tempDirCreated = tempDir.mkdir();
                }

                // проверка наличия
                if(!uploadDirCreated || !outputDirCreated || !tempDirCreated)
                {
                    return "Не удалось загрузить файл, т.к. файловая система не позволяет выполнить сохранение!";
                }

                // сохраняем файл на устройстве
                byte[] bytes = createSignFormModel.getFile().getBytes();
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
                stream.write(bytes);
                stream.close();

                // создаём изображение подписи
                signImageCreator.setImageGerbPath(signImageLogoPath);       // указываем путь к гербу
                signImageCreator.createSignImage(singImagePath, createSignFormModel); // создаём изображение подписи

                UploadedFileHandler documentHandler = null; // обработчик документов
                String outputFileName = null;
                // определяем тип полученного файла
                if (fileName.endsWith(".docx") || fileName.endsWith(".doc") || fileName.endsWith(".rtf"))
                {
                    documentHandler = wordHandler;
                }
                else if (fileName.endsWith(".pdf"))
                {
                    documentHandler = pdfHandler;
                }
                else if(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))
                {
                    documentHandler = excelHandler;
                }
                else
                {
                    return "Данный тип файлов не поддерживается!";
                }

                // обрабатываем полученный файл
                documentHandler.setSingImagePath(singImagePath); // указываем путь к картинке, которую нужно будет вставить
                documentHandler.setParams(createSignFormModel);    // указываем параметры обработки
                outputFileName = documentHandler.processDocument(fileName);   // запускаем обработку

                return "OK! http://localhost:8080/download?file=" + outputFileName;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return "Error! Не удалось загрузить " + fileName + " => " + e.getMessage();
            }
        }
        else
        {
            return "Error! Не удалось загрузить файл, потому что он пустой.";
        }
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ResponseBody
    public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "file") String fileName) throws IOException
    {
        File file = new File("output/" + fileName);
        if (file.exists())
        {

            //get the mimetype
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null)
            {
                //unknown mimetype so set the mimetype to application/octet-stream
                mimeType = "application/octet-stream";
            }

            response.setContentType(mimeType);

            /**
             * In a regular HTTP response, the Content-Disposition response header is a
             * header indicating if the content is expected to be displayed inline in the
             * browser, that is, as a Web page or as part of a Web page, or as an
             * attachment, that is downloaded and saved locally.
             *
             */

            /**
             * Here we have mentioned it to show inline
             */
            response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

            //Here we have mentioned it to show as attachment
            //response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + file.getName() + "\""));

            response.setContentLength((int) file.length());

            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

            FileCopyUtils.copy(inputStream, response.getOutputStream());
        }
    }
}
