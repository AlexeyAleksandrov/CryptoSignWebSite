package com.webapi.application.services.signImage;

import com.webapi.application.models.sign.CreateSignFormModel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class SignImageCreator
{
    // параметры картинки
    private static final int width = 384;    // ширина получаемой картинки
    private static final int height = 160;   // высота получаемой картинки

    // отступы с краёв для рисования рамки
    private static final int leftMargin = 10;    // отступ слева
    private static final int topMargin = 10;     // отступ сверху
    private static final int rightMargin = 20;   // отступ справа
    private static final int bottomMargin = 20;  // отступ снизу
    private static final int arcRadius = 20;     // радиус скругления с краёв

    // параметры рисования
    private static final int penWidth = 3;   // толщина пера
    private static final Color penColor = Color.BLACK;   // цвет рисования рамки
    private static final Color backgroundColor = Color.WHITE;    // цвет фона

    // параметры текста
    private static final int indent = 20;    // отступ по X от левого края рамки
    private static final int fontSizeBig = 17;   // размер шрифта для заголовков
    private static final int fontSizeSmall = 13; // размер шрифта для текста
    private static final String fontName = "Ubuntu";   // шрифт, которым будет написан текст

    // интервал между центрами строк текста
    private static final int interval = (height - (topMargin + bottomMargin)) / 5;    // шаг между строками, высота минус отступы, и делим на кол-во строк, которые нужно нарисовать

    // параметры герба
    private String imageGerbPath = null;     // путь к файлу герба
    private static final int imageGerbMarginLeft = 20;  // отступ слева для рисования герба
    private static final int imageGerbMarginTop = 10;   // отступ сверху для рисования герба
    private static final int imageGerbWidth = 52;   // ширина герба
    private static final int imageGerbHeight = 65;  // высота герба

    public void setImageGerbPath(String imageGerbPath)
    {
        this.imageGerbPath = imageGerbPath;
    }

    /**
     * Функция создания картинки с данными о подписи
     * @param filePath место сохранения итоговой картинки
     * @param owner строка, которая отображается в поле Владелец
     * @param certificate строка, которая отображается в поле сертификат
     * @param validFrom дата начала действия сертификата, строка
     * @param validTo дата окончания действия сертификата, строка
     * @param drawGerb включает и выключает отрисовку герба. Для работы нужно указать путь к файлу герба через {@link SignImageCreator#setImageGerbPath}
     * @throws IOException исключения, при ошибках чтения и записи файлов картинки или герба
     */
    public void createSignImage(String filePath, String owner, String certificate, String validFrom, String validTo, boolean drawGerb) throws IOException
    {
        CreateSignFormModel convertParams = new CreateSignFormModel();
        convertParams.setSignOwner(owner);
        convertParams.setSignCertificate(certificate);
        convertParams.setSignDateStart(validFrom);
        convertParams.setSignDateEnd(validTo);
        convertParams.setDrawLogo(drawGerb);

        createSignImage(filePath, convertParams);   // вызываем ту-же функцию, но с моделью параметров
    }

    /**
     * Функция создания картинки с данными о подписи
     * @param filePath место сохранения итоговой картинки
     * @param convertParams параметры изображения - сертификат, владелец и т.д.
     * @throws IOException исключения, при ошибках чтения и записи файлов картинки или герба
     */
    public void createSignImage(String filePath, CreateSignFormModel convertParams) throws IOException
    {
        String owner = convertParams.getSignOwner();
        String certificate = convertParams.getSignCertificate();
        String validFrom = convertParams.getSignDateStart();
        String validTo = convertParams.getSignDateEnd();
        boolean drawGerb = convertParams.isDrawLogo();

        File file = new File(filePath);

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) bi.getGraphics();
        graphics.setBackground(backgroundColor);
        graphics.clearRect(0, 0, width, height);
        graphics.setPaint(penColor);

        drawText(graphics, indent, interval, "ДОКУМЕНТ ПОДПИСАН", Font.BOLD, fontSizeBig, true);
        drawText(graphics, indent, interval * 2, "ЭЛЕКТРОННОЙ ПОДПИСЬЮ", Font.BOLD, fontSizeBig, true);
        drawText(graphics, indent, interval * 3, "Владелец: " + owner, Font.PLAIN, fontSizeSmall, false);
        drawText(graphics, indent, interval * 4, "Сертификат: " + certificate, Font.PLAIN, fontSizeSmall, false);
        drawText(graphics, indent, interval * 5, "Действителен с " + validFrom + " до " + validTo, Font.PLAIN, fontSizeSmall, false);

        graphics.setStroke(new BasicStroke(penWidth)); // ширина обводки
        graphics.draw(new RoundRectangle2D.Double(leftMargin, topMargin, width - rightMargin, height - bottomMargin, arcRadius, arcRadius));

        if(drawGerb)    // если надо рисовать герб
        {
            Image imageGerb = ImageIO.read(new File(imageGerbPath));    // загружаем изображение герба из файла
            graphics.drawImage(imageGerb, imageGerbMarginLeft, imageGerbMarginTop, imageGerbWidth, imageGerbHeight, null);    // рисуем
        }

        try
        {
            ImageIO.write(bi, "jpg", file);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // функция рисования текста на изображении
    public void drawText(Graphics2D graphics, int position_X, int position_Y, String text, int fontType, int fontSize, boolean drawInCenter)
    {
        Font font = new Font(fontName, fontType, fontSize); // шрифт текста
        graphics.setFont(font); // задаем шрифт рисовальщику

        // считаем координаты так, чтобы текст оказался по центру
        FontRenderContext context = graphics.getFontRenderContext();    // получаем контекст шрифта
        Rectangle2D bounds = font.getStringBounds(text, context);   // получаем на его основе прямоугольник, в котором будет располагаться заданный текст
        double x_center = (width - bounds.getWidth()) / 2;  // считаем координату X центра картинки
        double y_center = (bounds.getHeight()) / 2;

        int text_x = position_X;    // по умолчанию сохраняем стандартную позицию
        int text_y = position_Y + (int) y_center;    // делаем смещение по Y для центровки (так предлагается в документации)

        if (drawInCenter)    // если нужно нарисовать текст по центру
        {
            text_x = (int) x_center; // то заменяем координату по X на центр
        }

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  // устанавливаем флаги для сглаживания
        graphics.drawString(text, text_x, text_y);  // рисуем надпись
    }
}
