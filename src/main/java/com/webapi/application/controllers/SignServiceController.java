package com.webapi.application.controllers;

import com.webapi.application.models.sign.*;
import com.webapi.application.models.user.User;
import com.webapi.application.repositories.UsersRepository;
import com.webapi.application.services.cryptopro.jsp.CryptoPROCertificateModel;
import com.webapi.application.services.cryptopro.jsp.CryptoProSignService;
import com.webapi.application.services.handlers.Excel.ExcelHandler;
import com.webapi.application.services.handlers.PDF.PDFHandler;
import com.webapi.application.services.handlers.UploadedFileHandler;
import com.webapi.application.services.handlers.Word.WordHandler;
import com.webapi.application.services.sign.create.queue.blockingqueue.SignCreateTask;
import com.webapi.application.services.sign.create.queue.service.SignCreateQueueService;
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
import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor // внедрение зависимостей через конструктор
@RequestMapping("/sign")
public class SignServiceController
{
//    public static final String singImagePath = "temp/sign_image.jpg";   // путь сохранения готового изображения подписи
//    public static final String signImageLogoPath = "logo/mirea_gerb_52_65.png";  // путь к гербу для изображения

    // внедрение зависимостей через конструктор
//    final SignImageCreator signImageCreator;    // сервис создания изображения подписи
//    final WordHandler wordHandler;      // обработчик Word документов
//    final ExcelHandler excelHandler;    // обработчик Excel документов
//    final PDFHandler pdfHandler;    // обработчик PDF документов
    final CryptoProSignService cryptoProSignService;    // сервис создания подписей
    final SignCreateQueueService signCreateQueueService;    // сервис очереди задач

    // репозитории
    UsersRepository usersRepository;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String index(Model model, Authentication authentication, @RequestParam(name = "index", required = false, defaultValue = "-1") int index)
    {
        CreateSignFormModel createSignFormModel = null;
        if(authentication != null)  // если пользователь авторизован
        {
            // шаблоны
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = usersRepository.findByUsername(userDetails.getUsername()).orElse(null); // получаем пользователя

            if(user != null)    // если пользователь найден
            {
                model.addAttribute("login", user.getUsername());    // добавляем имя пользователя

                // загружаем список шаблонов пользователя
                List<SignTemplateModel> signTemplateModels = user.getSignTemplates();   // получаем список шаблонов
                model.addAttribute("signTemplateModels", signTemplateModels);   // отправляем список шаблонов

                if(index >= 0 && index < signTemplateModels.size()) // проверяем границы индекса
                {
                    createSignFormModel = signTemplateModels.get(index).toCreateSignFormModel();    // применяем выбранный шаблон
                }
            }
        }
        else
        {
            return "redirect:/auth";
        }

        if(createSignFormModel == null)   // если модель не была добавлена
        {
            createSignFormModel = new CreateSignFormModel();
        }

        createSignFormModel.setTemplate(true);  // задаем статус, что это шаблон

        model.addAttribute("signModel", createSignFormModel);   // передаём данные для заполнения на форму
        return "sign/service/create_from_template";
    }

    @RequestMapping(value = "/createRuToken", method = RequestMethod.GET)
    public String createSignFromRuToken(Model model, Authentication authentication,
                                        @RequestParam(name = "index", required = false, defaultValue = "-1") int index)
    {
        CreateSignFormModel createSignFormModel = null;
        if(authentication != null)  // если пользователь авторизован
        {
            // шаблоны
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = usersRepository.findByUsername(userDetails.getUsername()).orElse(null); // получаем пользователя

            if(user != null)    // если пользователь найден
            {
                model.addAttribute("login", user.getUsername());    // добавляем имя пользователя

                // TODO: Сделать выбор между название сертификата и владельцем
                // TODO: Сделать сохранения состояния галочек, при переключении сертификатов

                // подгружаем с токена
                List<CryptoPROCertificateModel> certificates = cryptoProSignService.getCertificates();
                List<RuTokenSignModel> ruTokenSignModels = new ArrayList<>();

                // преобразовываем сертификаты в модели
                for (CryptoPROCertificateModel cert : certificates)
                {
                    ruTokenSignModels.add(RuTokenSignModel.fromCryptoPROCertificateModel(cert));
                }

                // устанавливаем выбранный сертификат
                if(index >= 0 && index < ruTokenSignModels.size())
                {
                    createSignFormModel = CreateSignFormModel.fromRuTokenModel(ruTokenSignModels.get(index));
                }
                else if(ruTokenSignModels.size() > 0)   // если сертификат не выбран
                {
                    createSignFormModel = CreateSignFormModel.fromRuTokenModel(ruTokenSignModels.get(0));
                }

                model.addAttribute("ruTokenSignModels", ruTokenSignModels);   // отправляем список шаблонов
            }
        }
        else
        {
            return "redirect:/auth";
        }

        if(createSignFormModel == null)   // если модель не была добавлена
        {
            createSignFormModel = new CreateSignFormModel();
        }

        createSignFormModel.setTemplate(false);  // задаем статус, что это не шаблон

        model.addAttribute("signModel", createSignFormModel);   // передаём данные для заполнения на форму
        return "sign/service/create_from_rutoken";
    }

//    @RequestMapping(value = "/upload", method = RequestMethod.GET)
//    public @ResponseBody
//    String provideUploadInfo()
//    {
//        return "Вы можете загружать файл с использованием того же URL.";
//    }

    // TODO: Сделать загрузку не одного, а несколько файлов
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String handleFileUpload(@ModelAttribute("signModel") CreateSignFormModel createSignFormModel, Authentication authentication, Model model)
    {
        if(createSignFormModel == null)
        {
            return "redirect:/sign/create";
        }   // TODO: Сделать стилизацию input с файлами, шаблон -> https://snipp.ru/html-css/input-file-style

        if(authentication == null)  // если пользователь не авторизован
        {
            return "redirect:/auth";
        }

        // получаем пользователя
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = usersRepository.findByUsername(userDetails.getUsername()).orElse(null); // получаем пользователя

        if (user == null)    // если пользователь не найден
        {
            return "redirect:/auth";
        }

        model.addAttribute("login", user.getUsername());    // добавляем имя пользователя

        SignResultDownloadModel resultDownloadModel = new SignResultDownloadModel();    // результат
        model.addAttribute("result", resultDownloadModel);      // добавляем аттрибут

        resultDownloadModel.setFileName(createSignFormModel.getFile().getOriginalFilename());     // задаем название файла
        resultDownloadModel.setDigitalSign(false);      // ставим, что цифровой подписи нет

        if (!createSignFormModel.isTemplate()) // если мы работаем с реальной подписью
        {
            // TODO: Связать пользователя с доступными ему сертификатами
            // TODO: Сделать перезагрузку списка сертификатов

            CryptoPROCertificateModel cert = cryptoProSignService.getCertificateBySerialNumber(createSignFormModel.getSignCertificate());   // получаем сертификат по его серийному номеру
            if(cert == null)
            {
                resultDownloadModel.setStatus(FileProcessingResultStatus.ERROR_CERTIFICATE_NOT_FOUND);
                resultDownloadModel.setErrorMessage("Сертификат не найден!");
                return "sign/service/result_download_document";
//                return "Error! Сертификат не найден!";
            }

            if(createSignFormModel.getDisplayNameType() == 1)   // если вместо владельца сертификата, нужно использовать его название
            {
                createSignFormModel.setSignOwner(cert.getCertificateName());    // заменяем владельца на название
            }
        }   // TODO: Вынести обработку в отдельный поток со списком "task" и JS страница с таймаутом

        createSignFormModel.setFileName(createSignFormModel.getFile().getOriginalFilename());
        final String currentDir = System.getProperty("user.dir");
        String fileName = currentDir + "/uploadedfiles/" + createSignFormModel.getFile().getOriginalFilename();   // получаем оригинальное название файла, который был загружен

        // проверка корректности входных данных
        if(createSignFormModel.getInsertType() == -1
                || (createSignFormModel.getInsertType() == 2
                && !(fileName.endsWith(".docx") || fileName.endsWith(".doc") || fileName.endsWith(".rtf"))))
        {
            resultDownloadModel.setStatus(FileProcessingResultStatus.ERROR_FILE_NOT_SUPPORTING_FOR_TAG);
            resultDownloadModel.setErrorMessage("Выбранный тип подписи не подходит для данного типа файлов!");
            return "sign/service/result_download_document";
//            return "Error! Выбранный тип подписи не подходит для данного типа файлов!";
        }

        // проверяем файл
        if (createSignFormModel.getFile().isEmpty())
        {
            resultDownloadModel.setStatus(FileProcessingResultStatus.ERROR_FILE_NOT_SAVED);
            resultDownloadModel.setErrorMessage("Не удалось загрузить файл, потому что он пустой");
            return "sign/service/result_download_document";
            //            return "Error! Не удалось загрузить файл, потому что он пустой.";
        }

        // начинаем обработку файла

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
            resultDownloadModel.setStatus(FileProcessingResultStatus.ERROR_FILE_NOT_SAVED);
            resultDownloadModel.setErrorMessage("Не удалось загрузить файл, т.к. файловая система не позволяет выполнить сохранение!");
            return "sign/service/result_download_document";
//                    return "Не удалось загрузить файл, т.к. файловая система не позволяет выполнить сохранение!";
        }

        // сохраняем файл на устройстве
        try
        {
            byte[] bytes = createSignFormModel.getFile().getBytes();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
            stream.write(bytes);
            stream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();

            resultDownloadModel.setStatus(FileProcessingResultStatus.ERROR_FILE_NOT_SAVED);
            resultDownloadModel.setErrorMessage("Не удалось загрузить " + fileName + " => " + e.getMessage());
            return "sign/service/result_download_document";
            //                return "Error! Не удалось загрузить " + fileName + " => " + e.getMessage();
        }

        // настраиваем обработчик документов
        UploadedFileHandler documentHandler; // обработчик документов
        // определяем тип полученного файла
        if (!(fileName.endsWith(".docx") || fileName.endsWith(".doc") || fileName.endsWith(".rtf") || fileName.endsWith(".pdf") || fileName.endsWith(".xlsx") || fileName.endsWith(".xls")))
        {
            resultDownloadModel.setStatus(FileProcessingResultStatus.ERROR_FILE_NOT_SUPPORTING);
            resultDownloadModel.setErrorMessage("Данный тип файлов не поддерживается!");
            return "sign/service/result_download_document";
        }

        CryptoPROCertificateModel cert = null;
        if(!createSignFormModel.isTemplate())    // если мы работаем с реальной подписью
        {
            cert = cryptoProSignService.getCertificateBySerialNumber(createSignFormModel.getSignCertificate());   // получаем сертификат по его серийному номеру
        }

        String outputFileName = "";
        signCreateQueueService.addTask(user.getId(), fileName, currentDir, cert, createSignFormModel);      // добавляем задачу в очередь


//        SignCreateTask task = new SignCreateTask(0L, 0L, fileName, currentDir, cert, createSignFormModel, signImageCreator, documentHandler, cryptoProSignService);

//        // создаём изображение подписи
//        signImageCreator.setImageGerbPath(signImageLogoPath);       // указываем путь к гербу
//        signImageCreator.createSignImage(singImagePath, createSignFormModel); // создаём изображение подписи

        // обрабатываем полученный файл
//        documentHandler.setSingImagePath(singImagePath); // указываем путь к картинке, которую нужно будет вставить
//        documentHandler.setParams(createSignFormModel);    // указываем параметры обработки
//        outputFileName = documentHandler.processDocument(fileName);   // запускаем обработку

        if(createSignFormModel.isTemplate())    // если мы работаем с шаблонами
        {
            resultDownloadModel.setStatus(FileProcessingResultStatus.OK);
            resultDownloadModel.setFileDownloadLink("http://localhost:8080/sign/download?file=" + outputFileName);
            return "sign/service/result_download_document";
//                    return "OK! http://localhost:8080/sign/download?file=" + outputFileName;
        }
        else    // если мы работаем с реальной подписью
        {
            try
            {
                // создаём подпись
//                String fileForSignName = currentDir + "/output/" + outputFileName;   // получаем оригинальное название файла, который был загружен
//                CryptoPROCertificateModel cert = cryptoProSignService.getCertificateBySerialNumber(createSignFormModel.getSignCertificate());   // получаем сертификат по его серийному номеру
//                cryptoProSignService.createSign(fileForSignName, cert, "12345678", true);   // создаём подпись

                resultDownloadModel.setStatus(FileProcessingResultStatus.OK);
                resultDownloadModel.setDigitalSign(true);      // ставим, что есть цифровая подпись
                resultDownloadModel.setFileDownloadLink("http://localhost:8080/sign/download?file=" + outputFileName);
                resultDownloadModel.setSignFileDownloadLink("http://localhost:8080/sign/download?file=" + outputFileName + ".sig");
                return "sign/service/result_download_document";
//                        return "OK! http://localhost:8080/sign/download?file=" + outputFileName + "\n http://localhost:8080/sign/download?file=" + outputFileName + ".sig";
            }
            catch (java.security.ProviderException providerException)   // ошибка лицензии
            {
                resultDownloadModel.setStatus(FileProcessingResultStatus.ERROR_CRYPTO_PRO_EXCEPTION);
                resultDownloadModel.setErrorMessage("Ошибка КриптоПРО JSP: " + providerException.getMessage());
                return "sign/service/result_download_document";
//                        return "Error! Ошибка КриптоПРО JSP: " + providerException.getMessage();
            }
        }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//
//            resultDownloadModel.setStatus(FileProcessingResultStatus.ERROR_FILE_NOT_SAVED);
//            resultDownloadModel.setErrorMessage("Не удалось загрузить " + fileName + " => " + e.getMessage());
//            return "sign/service/result_download_document";
////                return "Error! Не удалось загрузить " + fileName + " => " + e.getMessage();
//        }
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
