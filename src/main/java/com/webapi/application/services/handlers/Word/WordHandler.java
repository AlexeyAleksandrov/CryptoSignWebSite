package com.webapi.application.services.handlers.Word;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import com.webapi.application.services.handlers.UploadedFileHandler;
import com.webapi.application.services.handlers.PDF.PDFHandlerException;
import com.webapi.application.services.libreoffice.DocumentConverter;
import com.webapi.application.services.msword.OpenXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WordHandler extends UploadedFileHandler
{
    @Autowired
    OpenXML wordOpenXML;

    @Autowired
    DocumentConverter libreOffice;

    @Override
    public String processDocument(String fileName) throws PDFHandlerException, IOException, WordHandlerException, BootstrapException, Exception
    {
        if(!fileName.endsWith(".docx") && !fileName.endsWith(".doc") && !fileName.endsWith(".rtf"))
        {
            throw new WordHandlerException("Данный тип файлов не поддерживается! Допустимое расширение файла: *.docx");
        }

        // конвертируем в *.docx
        if(fileName.endsWith(".doc") || fileName.endsWith(".rtf"))
        {
//            DocumentConverter libreOffice = new DocumentConverter();
            String outputFile = fileName.replace(".doc", ".docx");
            outputFile = outputFile.replace(".rtf", ".docx");
            libreOffice.convertTo(fileName, outputFile, DocumentConverter.ConvertType.CONVERT_TO_DOCX);
            fileName = outputFile;
        }

        // вставляем картинку
//        OpenXML wordOpenXML = new OpenXML();    // создаем обработчик OpenXML документов
        if(params.getInsertType() == 0)     // если используется классическая вставка
        {
            wordOpenXML.insertImageToWord(fileName, fileName, singImagePath);   // делаем вставку в конец документа
        }
        else if (params.getInsertType() == 2)
        {
            wordOpenXML.insertImageByTag(fileName, fileName, singImagePath, params.getSignOwner()); // делаем вставку по тэгу
        }
        else
        {
            throw new WordHandlerException("Указан некорректный тип добавления подписи!");
        }

        // экспортируем в PDF
        DocumentConverter libreOffice = new DocumentConverter();
        String outputFileName = fileName.replace(".docx", ".pdf");
        outputFileName = outputFileName.replace("uploadedfiles", "output");
        libreOffice.convertTo(fileName, outputFileName, DocumentConverter.ConvertType.EXPORT_TO_PDF);

        outputFileName = params.getFileName()
                .replace(".docx", ".pdf")
                .replace(".doc", ".pdf")
                .replace(".rtf", ".pdf");
        return outputFileName;
    }
}
