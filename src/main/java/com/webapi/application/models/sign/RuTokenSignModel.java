package com.webapi.application.models.sign;

import com.webapi.application.services.cryptopro.jsp.CryptoPROCertificateModel;
import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        model.setCertificateName(cert.getCertificateName());    // название сертификата
        model.setSignOwner(cert.getOwner());   // владелец
        model.setSignCertificate(cert.getCertificateSerialNumber());  // номер сертификата
        model.setSignDateStart(dateFormat.format(cert.getValidFrom()));   // дата начал действия сертификата
        model.setSignDateEnd(dateFormat.format(cert.getValidTo()));   // дата окончания действия сертификата

        return model;
    }
}
