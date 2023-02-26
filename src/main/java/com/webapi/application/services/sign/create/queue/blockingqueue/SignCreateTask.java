package com.webapi.application.services.sign.create.queue.blockingqueue;

import com.sun.star.comp.helper.BootstrapException;
import com.webapi.application.models.sign.CreateSignFormModel;
import com.webapi.application.models.sign.FileProcessingResultStatus;
import com.webapi.application.services.cryptopro.jsp.CryptoPROCertificateModel;
import com.webapi.application.services.cryptopro.jsp.CryptoProSignService;
import com.webapi.application.services.handlers.Excel.ExcelHandlerException;
import com.webapi.application.services.handlers.PDF.PDFHandlerException;
import com.webapi.application.services.handlers.UploadedFileHandler;
import com.webapi.application.services.handlers.Word.WordHandlerException;
import com.webapi.application.services.signImage.SignImageCreator;
import lombok.AllArgsConstructor;

import java.io.IOException;

public class SignCreateTask implements Runnable
{
    private Long taskId;    // id задачи
    private Long userId;    // id пользователя
    private String fileName;    // файл, с которым работаем
    private String currentDir;      // текущая директория
    private CryptoPROCertificateModel cert;     // сертификат
    private CreateSignFormModel createSignFormModel;    // текстовые данные о подписи
    private SignImageCreator signImageCreator;      // сервис создания изображения подписи
    private UploadedFileHandler documentHandler;    // обработчик документов
    private CryptoProSignService cryptoProSignService;    // сервис создания подписей
    private String outputFileName;      // название файла вывода (передается по ссылке, в него выводится значение)

    public SignCreateTask(Long taskId, Long userId, String fileName, String currentDir, CryptoPROCertificateModel cert, CreateSignFormModel createSignFormModel, SignImageCreator signImageCreator, UploadedFileHandler documentHandler, CryptoProSignService cryptoProSignService)
    {
        this.taskId = taskId;
        this.userId = userId;
        this.fileName = fileName;
        this.currentDir = currentDir;
        this.cert = cert;
        this.createSignFormModel = createSignFormModel;
        this.signImageCreator = signImageCreator;
        this.documentHandler = documentHandler;
        this.cryptoProSignService = cryptoProSignService;
    }

    @Override
    public void run()
    {
        try
        {
            signImageCreator.createSignImage(createSignFormModel); // создаём изображение подписи
            documentHandler.setParams(createSignFormModel);    // указываем параметры обработки
            outputFileName = documentHandler.processDocument(fileName);   // запускаем обработку

            if(!createSignFormModel.isTemplate())    // если мы работаем с реальной подписью
            {
                // создаём подпись
                String fileForSignName = currentDir + "/output/" + outputFileName;   // получаем оригинальное название файла, который был загружен
                cryptoProSignService.createSign(fileForSignName, cert, "12345678", true);   // создаём подпись
            }

            System.out.println("Готово! " + outputFileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getOutputFileName()
    {
        return outputFileName;
    }
}
