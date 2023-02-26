package com.webapi.application.services.handlers.PDF;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import com.webapi.application.services.handlers.Excel.ExcelHandlerException;
import com.webapi.application.services.handlers.UploadedFileHandler;
import com.webapi.application.services.handlers.Word.WordHandlerException;
import com.webapi.application.services.pdfbox.PDFBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class PDFHandler extends UploadedFileHandler
{
    @Override
    public void processDocument(String fileName, String outputFileName) throws PDFHandlerException, IOException, WordHandlerException, BootstrapException, Exception, ExcelHandlerException
    {
        if(!fileName.toLowerCase().endsWith(".pdf"))
        {
            throw new PDFHandlerException("Файл должен быть формата PDF");
        }

        //Loading an existing document
        File file = new File(fileName);
        PDDocument doc = PDDocument.load(file);

        BufferedImage image = PDFBox.getImageLastPagePdfDocument(doc);

        // получаем геометрию страницы
        int width = image.getWidth();
        int height = image.getHeight();

        double widthCoefficient = (double)width/(double)PDFBox.pageReferenceWidth;  // коэффициент маштабирования для ширины страницы
        double heightCoefficient = (double)height/(double)PDFBox.pageReferenceHeight;   //коэффициент маштабирования для высоты страницы

        System.out.println("w = " + width + " h = " + height);

        int lastLine_YpPos = 0;

        for (int y = height - 1; y >= 0; y--)  // идём с конца по высоте
        {
            boolean existLine = false;  // флаг того, что строка с пикселем не белого цвета найдена
            for (int x = 0; x < width; x++)
            {
                int argb = image.getRGB(x, y);  // получение цветов, взято из документации
                int red = (argb >> 16) & 0xff;
                int green = (argb >> 8) & 0xff;
                int blue = (argb) & 0xff;

                if(red != 255 && green != 255 & blue != 255)    // условие, что пиксель не белого цвета
                {
                    lastLine_YpPos = y;
                    existLine = true;
                    break;
                }
            }
            if(existLine)
            {
                break;
            }
        }

        int imageWidth = (int)(((double)PDFBox.imageWidth) * widthCoefficient); // ширина изображения для конкретного PDF файла
        int imageHeight = (int)(((double)PDFBox.imageHeight) * heightCoefficient); // высота изображения для конкретного PDF файла
        imageHeight = (int)(((double)PDFBox.imageHeight) * widthCoefficient);   // для вертикального соотношения

        // считаем координаты для вставки изображения в центр конца документа
        int x = width/2 - imageWidth/2;    // центр по ширине
        int y = height - lastLine_YpPos - imageHeight - PDFBox.verticalOffset; // делаем вычитание координаты Y из высоты, т.к. при рисовании система координат обратная

        final int upDownBorder = (int)((double)height * (double)0.0675);  // вычисляем верхнюю/нижнюю границу (поля отступа) (6.75% = 2 см), нижний отступ

        if(y < upDownBorder)  // если Y ниже нижней границы (уходит на нижнюю страницу)
        {
            System.out.println("Произошёл переход на новую страницу!");
            PDRectangle pageRectangle = doc.getPage(doc.getNumberOfPages()-1).getMediaBox();    // получаем габаритные размеры последней страницы
            PDPage page = new PDPage(pageRectangle);    // создаём новую страницу такого же размера
            doc.addPage(page);

            y = height - imageHeight - PDFBox.verticalOffset - upDownBorder;   // пересчитываем высоту так, чтобы картинка теперь оказалась наверху
        }

        PDFBox.drawImageOnEndOfDocument(doc, outputFileName, singImagePath, x, y, imageWidth, imageHeight);    // рисуем картинку по координатам

        System.out.println(" " + x + " " + y);

        doc.close();
    }

    @Override
    public String processDocument(String fileName) throws PDFHandlerException, IOException, ExcelHandlerException, WordHandlerException, BootstrapException, Exception
    {
        String fileNameOutput= currentDir + "/output/" + params.getFileName();    // указываю папку вывода
        processDocument(fileName, fileNameOutput);  // обрабатываю документ
        return params.getFileName();    // возвращаю название исходного файла
    }
}
