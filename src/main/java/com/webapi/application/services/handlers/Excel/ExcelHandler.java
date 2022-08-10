package com.webapi.application.services.handlers.Excel;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import com.webapi.application.controllers.FileUploadController;
import com.webapi.application.services.handlers.PDF.PDFHandler;
import com.webapi.application.services.handlers.PDF.PDFHandlerException;
import com.webapi.application.services.handlers.UploadedFileHandler;
import com.webapi.application.services.handlers.Word.WordHandlerException;
import com.webapi.application.services.libreoffice.DocumentConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ExcelHandler  extends UploadedFileHandler
{
    @Autowired
    DocumentConverter libreOffice;

    @Autowired
    PDFHandler pdfHandler;

    public ExcelHandler()
    {
        singImagePath = FileUploadController.singImagePath;  // путь к картинке, которую надо вставить
    }

    @Override
    public String processDocument(String fileName) throws PDFHandlerException, IOException, WordHandlerException, BootstrapException, Exception, ExcelHandlerException
    {
        if(!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))
        {
            throw new ExcelHandlerException("Данный тип файлов не поддерживается! Допуступные расширения файлов: *.xlsx и *.xls");
        }

        // экспортируем в PDF
//        DocumentConverter libreOffice = new DocumentConverter();
        String outputPdfFileName = fileName.replace(".xlsx", ".pdf").replace(".xls", ".pdf");
//        outputPdfFileName = outputPdfFileName.replace("uploadedfiles", "output");
        libreOffice.convertTo(fileName, outputPdfFileName, DocumentConverter.ConvertType.EXPORT_TO_PDF);

        String outputFileName = outputPdfFileName.replace("uploadedfiles", "output");   // заменяем название папки вывода

        // обрабатываем документ как PDF
//        PDFHandler pdfHandler = new PDFHandler();   // обработчик PDF
        pdfHandler.setSingImagePath(singImagePath);
        pdfHandler.processDocument(outputPdfFileName, outputFileName);

        outputFileName = params.getFileName()
                .replace(".xlsx", ".pdf")
                .replace(".xls", ".pdf");
        return outputFileName;
    }

}
