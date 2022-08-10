package com.webapi.application.services.msword;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

@Service
public class OpenXML
{
    public boolean insertImageToWord(String inputFileName, String outFileName, String imageFileName)
    {
        try
        {
            File inFile = new File(inputFileName);
            if (!inFile.exists())
            {
                System.out.println("Файл документа не найден!");
                return false;
            }

            File imageFile = new File(imageFileName);
            if (!inFile.exists())
            {
                System.out.println("Файл картинки не найден!");
                return false;
            }

            // получаем габариты картинки
            BufferedImage bimg1 = ImageIO.read(imageFile);
            int width = bimg1.getWidth();
            int height = bimg1.getHeight();
            double w = width;
            double h = height;
            double scale_factor = 2.25; // коэффициент, который даёт габариты картинки 2.5 * 6.0 см
            w /= scale_factor;
            h /= scale_factor;
            width = (int) w;
            height = (int) h;

            int format = XWPFDocument.PICTURE_TYPE_JPEG;

            if (inputFileName.endsWith(".docx"))
            {
                XWPFDocument doc = new XWPFDocument(new FileInputStream(inputFileName));
                XWPFParagraph p = doc.createParagraph();    // создаём параграф
                p.setAlignment(ParagraphAlignment.CENTER);  // выравниваем по центру
                XWPFRun xwpfRun = p.createRun();
                //                xwpfRun.addBreak();   // добавить перенос строки
                xwpfRun.addPicture(new FileInputStream(imageFileName), format, imageFileName, Units.toEMU(width), Units.toEMU(height)); // вставляем картинку

                FileOutputStream out = new FileOutputStream(outFileName);
                doc.write(out);
                out.close();
            }
        }
        catch (FileNotFoundException exception)
        {
            System.out.println("Не удалось открыть файл: " + exception);
            return false;
        }
        catch (IOException exception)
        {
            System.out.println("Произошла ошибка при обработке файла: " + exception);
            return false;
        }
        catch (InvalidFormatException e)
        {
            e.printStackTrace();
            System.out.println("Не удалось добавить картинку в документ");
            return false;
        }
        System.out.println("Картинка успешно добавлена");
        return true;
    }

    public boolean insertImageByTag(String inputFileName, String outFileName, String imageFileName, String signOwner)
    {
        try
        {
            File inFile = new File(inputFileName);
            if (!inFile.exists())
            {
                System.out.println("Файл документа не найден!");
                return false;
            }

            File imageFile = new File(imageFileName);
            if (!inFile.exists())
            {
                System.out.println("Файл картинки не найден!");
                return false;
            }

            // получаем габариты картинки
            BufferedImage bimg1 = ImageIO.read(imageFile);
            int width = bimg1.getWidth();
            int height = bimg1.getHeight();
            double w = width;
            double h = height;
            double scale_factor = 2.25; // коэффициент, который даёт габариты картинки 2.5 * 6.0 см
            w /= scale_factor;
            h /= scale_factor;
            width = (int) w;
            height = (int) h;

            int format = XWPFDocument.PICTURE_TYPE_JPEG;

            if (inputFileName.endsWith(".docx"))
            {
                XWPFDocument doc = new XWPFDocument(new FileInputStream(inputFileName));

                List<XWPFTable> table = doc.getTables();

                for (XWPFTable xwpfTable : table)
                {
                    List<XWPFTableRow> row = xwpfTable.getRows();
                    for (XWPFTableRow xwpfTableRow : row)
                    {
                        List<XWPFTableCell> cell = xwpfTableRow.getTableCells();
                        for (XWPFTableCell xwpfTableCell : cell)
                        {
                            if (xwpfTableCell != null)
                            {
                                String cell_text = xwpfTableCell.getText();

                                if (cell_text.contains("<имя_владельца_подписи>"))
                                {
                                    xwpfTableCell.removeParagraph(0);  // сбрасываем текст
                                    XWPFParagraph paragraph = xwpfTableCell.addParagraph();
                                    xwpfTableCell.setText(signOwner);
                                }
                                else if (cell_text.contains("<место_для_подписи>"))
                                {
                                    xwpfTableCell.removeParagraph(0);  // сбрасываем текст
                                    XWPFParagraph paragraph = xwpfTableCell.addParagraph();
                                    XWPFRun run = paragraph.createRun();

                                    run.addPicture(new FileInputStream(imageFileName), format, imageFileName, Units.toEMU(width), Units.toEMU(height)); // вставляем картинку
                                }
                            }
                        }
                    }
                }

                FileOutputStream out = new FileOutputStream(outFileName);
                doc.write(out);
                out.close();
            }
        }

        catch (FileNotFoundException exception)
        {
            exception.printStackTrace();
        }
        catch (IOException exception)
        {
            System.out.println("Произошла ошибка при обработке файла: " + exception);
            return false;
        }
        catch (InvalidFormatException e)
        {
            e.printStackTrace();
            System.out.println("Не удалось добавить картинку в документ");
            return false;
        }
        System.out.println("Картинка успешно добавлена");
        return true;
    }
}
