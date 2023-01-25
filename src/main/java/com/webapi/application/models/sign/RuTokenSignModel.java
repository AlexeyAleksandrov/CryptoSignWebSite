package com.webapi.application.models.sign;

import com.webapi.application.services.cryptopro.jsp.CryptoPROCertificateModel;
import lombok.Data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Модель сертификата, считанного с РуТокен, для представления в списке доступных вариантов
 */
@Data
public class RuTokenSignModel
{
    private String certificateName; // название сертификата
    private String signOwner;       // поле владельца подписи
    private String signCertificate; // поле номер сертификата
    private String signDateStart;   // поле начала действия сертификата
    private String signDateEnd;     // поле окончания действия сертификата

    /** Конвертер сертификата КриптоПРО в модель РуТокен для представления
     * @param cert сертификат КриптоПРО
     * @return модель РуТокен данных для представления в списке доступных вариантов
     */
    public static RuTokenSignModel fromCryptoPROCertificateModel(CryptoPROCertificateModel cert)
    {
        RuTokenSignModel model = new RuTokenSignModel();    // модель
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        model.setCertificateName(cert.getCertificateName());    // название сертификата
        model.setSignOwner(cert.getOwner());   // владелец
        model.setSignCertificate(cert.getCertificateSerialNumber());  // номер сертификата
        model.setSignDateStart(dateFormat.format(cert.getValidFrom()));   // дата начал действия сертификата
        model.setSignDateEnd(dateFormat.format(cert.getValidTo()));   // дата окончания действия сертификата

        return model;
    }

    /** Функция преобразования даты в формате представления для HTML формы в формат для документов
     * @param dateString исходная строка даты в формате yyyy-MM-dd
     * @return дата в формате dd.MM.yyyy
     */
    private String getDateStartInDocumentFormat(String dateString)
    {
        DateFormat dateFormFormat = new SimpleDateFormat("yyyy-MM-dd");     // форматер исходной строки
        DateFormat dateDocumentFormat = new SimpleDateFormat("dd.MM.yyyy"); // форматер конечной строки
        try
        {
            Date date = dateFormFormat.parse(dateString);   // получаем дату из исходной строки
            return dateDocumentFormat.format(date);     // возвращаем отформатированную дату
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return dateString;      // в случае ошибки, возвращаем ту же самую строку
        }
    }

    /**
     * @return Преобразовывает дату из yyyy-MM-dd в dd.MM.yyyy
     */
    public String getSignDateStartInDocumentFormat()
    {
        return getDateStartInDocumentFormat(signDateStart);
    }

    /**
     * @return Преобразовывает дату из yyyy-MM-dd в dd.MM.yyyy
     */
    public String getSignDateEndInDocumentFormat()
    {
        return getDateStartInDocumentFormat(signDateEnd);
    }
}
