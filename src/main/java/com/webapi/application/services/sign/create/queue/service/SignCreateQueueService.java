package com.webapi.application.services.sign.create.queue.service;

import com.webapi.application.models.sign.CreateSignFormModel;
import com.webapi.application.services.cryptopro.jsp.CryptoPROCertificateModel;
import com.webapi.application.services.cryptopro.jsp.CryptoProSignService;
import com.webapi.application.services.handlers.Excel.ExcelHandler;
import com.webapi.application.services.handlers.PDF.PDFHandler;
import com.webapi.application.services.handlers.UploadedFileHandler;
import com.webapi.application.services.handlers.Word.WordHandler;
import com.webapi.application.services.sign.create.queue.blockingqueue.SignCreateBlockingQueue;
import com.webapi.application.services.sign.create.queue.blockingqueue.SignCreateTask;
import com.webapi.application.services.signImage.SignImageCreator;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class SignCreateQueueService
{
    public static final String signImagePath = "temp/sign_image.jpg";   // путь сохранения готового изображения подписи
    public static final String signImageLogoPath = "logo/mirea_gerb_52_65.png";  // путь к гербу для изображения

    // внедрение зависимостей через конструктор
    private final SignImageCreator signImageCreator;    // сервис создания изображения подписи
    private final CryptoProSignService cryptoProSignService;    // сервис создания подписей

    private final WordHandler wordHandler;      // обработчик Word документов
    private final ExcelHandler excelHandler;    // обработчик Excel документов
    private final PDFHandler pdfHandler;    // обработчик PDF документов

    @Getter
    private final SignCreateBlockingQueue blockingQueue;  // блокирующая очередь задач на подпись
    @Getter
    private Long currentTaskId = 0L;    // id текущей задачи

    public SignCreateQueueService(SignImageCreator signImageCreator, CryptoProSignService cryptoProSignService, WordHandler wordHandler, ExcelHandler excelHandler, PDFHandler pdfHandler, SignCreateBlockingQueue blockingQueue)
    {
        this.signImageCreator = signImageCreator;
        this.cryptoProSignService = cryptoProSignService;
        this.wordHandler = wordHandler;
        this.excelHandler = excelHandler;
        this.pdfHandler = pdfHandler;
        this.blockingQueue = blockingQueue;

        signImageCreator.setImagePath(signImagePath);
        signImageCreator.setImageGerbPath(signImageLogoPath);

        Thread worker = new Thread(new Runnable()   // поток для выполнения задач
        {
            @Override
            public void run()
            {
                while (true)
                {
                    SignCreateTask task = SignCreateQueueService.this.blockingQueue.getNextTask();  // получаем следующую задачу на выполнение
                    currentTaskId = task.getTaskId();   // сохраняем ID текущей задачи
                    task.run();  // запускаем задачу на выполнение
                }
            }
        });

        worker.start();     // запускаем поток для выполнения задач
    }

    public void addTask(Long userId, String fileName, String currentDir, CryptoPROCertificateModel cert, CreateSignFormModel createSignFormModel)
    {
        // настраиваем обработчик документов
        UploadedFileHandler documentHandler = null; // обработчик документов

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

        if(documentHandler != null)
        {
            documentHandler.setSingImagePath(signImagePath);
            documentHandler.setCurrentDir(currentDir);
        }

        Long taskId = blockingQueue.getLastTaskId();    // получаем ID для задачи
        SignCreateTask task = new SignCreateTask(taskId, userId, fileName, currentDir, cert, createSignFormModel, signImageCreator, documentHandler, cryptoProSignService);
        blockingQueue.addTask(task);    // добавляем задачу в очередь
    }
}
