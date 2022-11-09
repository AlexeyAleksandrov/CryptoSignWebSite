package com.webapi.application.services.pdfbox;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

@Service
public class PDFBox
{
    public static final int imageHeight = 78; // высота картинки
    public static final int imageWidth = 179;  // ширина картинки
    public static final int pageReferenceHeight = 841;  // эталонная высота страницы А4
    public static final int pageReferenceWidth = 595;   // эталонная ширина страницы А4
    public static final int verticalOffset = 5;   // смещение по пикселям вниз, от текста

    public static void drawImageOnEndOfDocument(PDDocument doc, String filePathOutput, String imagePath, int pos_x, int pos_y, int imageWidth, int imageHeight) throws IOException
    {
//        //Loading an existing document
//        File file = new File(filePathInput);
//        PDDocument doc = PDDocument.load(file);

        //Retrieving the page
        PDPage page = doc.getPage(doc.getNumberOfPages() - 1);

        //Creating PDImageXObject object
        PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, doc);

        //creating the PDPageContentStream object
        PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false);

        //Drawing the image in the PDF document
        contents.drawImage(pdImage, pos_x, pos_y, imageWidth, imageHeight);

        System.out.println("Image inserted");

        //Closing the PDPageContentStream object
        contents.close();

        //Saving the document
        doc.save(filePathOutput);

//        //Closing the document
//        doc.close();

    }

    // функция получения изображения последней страницы PDF документа
    public static BufferedImage getImageLastPagePdfDocument(PDDocument doc) throws IOException
    {
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage image = renderer.renderImage(doc.getNumberOfPages() - 1);

        return image;
    }

    public void main(String args[]) throws Exception
    {
        String fileNameInput = "C:\\Users\\ASUS\\Downloads\\docs_conv\\титульник_pdf.pdf";
        String fileNameOutput= "C:\\Users\\ASUS\\Downloads\\docs_conv\\титульник_pdf_out.pdf";
        String imageFileName = "C:\\Users\\ASUS\\Pictures\\image_java.jpg";

        //Loading an existing document
        File file = new File(fileNameInput);
        PDDocument doc = PDDocument.load(file);

        BufferedImage image = getImageLastPagePdfDocument(doc);

        // получаем геометрию страницы
        int width = image.getWidth();
        int height = image.getHeight();

        double widthCoefficient = (double)width / (double)PDFBox.imageWidth;
        double heightCoefficient = (double)height / (double)PDFBox.imageHeight;

        int imageWidth = (int)((double)PDFBox.imageWidth * widthCoefficient);
        int imageHeight = (int)((double)PDFBox.imageHeight * heightCoefficient);

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

        // считаем координаты для вставки изображения в центр конца документа
        int x = width/2 - imageWidth/2;    // центр по ширине
        int y = height - lastLine_YpPos - imageHeight - verticalOffset; // делаем вычитание координаты Y из высоты, т.к. при рисовании система координат обратная

        final int upDownBorder = (int)((double)height * (double)0.0675);  // вычисляем верхнюю/нижнюю границу (поля отступа) (6.75% = 2 см), нижний отступ

        if(y < upDownBorder)  // если Y ниже нижней границы (уходит на нижнюю страницу)
        {
            System.out.println("Произошёл переход на новую страницу!");
            PDPage page = new PDPage();
            doc.addPage(page);

            y = height - imageHeight - verticalOffset - upDownBorder;   // пересчитываем высоту так, чтобы картинка теперь оказалась наверху
        }

        drawImageOnEndOfDocument(doc, fileNameOutput, imageFileName, x, y, imageWidth, imageHeight);    // рисуем картинку по координатам

        System.out.println(" " + x + " " + y);

        doc.close();
    }
}
