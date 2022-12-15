package com.webapi.application.services.cryptopro.jsp;

import org.springframework.stereotype.Service;

import java.security.cert.Certificate;
import java.util.List;

@Service
public class CryptoProSignService
{
    /**
     * Обработчик Крипто ПРО
     */
    private CryptoPRO_JSP cryptoPRO_jsp;    // обработчик Крипто ПРО
    /**
     * Список загруженных сертификатов
     */
    private List<CryptoPROCertificateModel> certificates;

    /** Конструктор
     * @param cryptoPRO_jsp Внедряемая зависимость - обработчик КриптоПРО
     */
    public CryptoProSignService(CryptoPRO_JSP cryptoPRO_jsp)
    {
        this.cryptoPRO_jsp = cryptoPRO_jsp;
        this.cryptoPRO_jsp.loadRuTokenCertificates();
        certificates = this.cryptoPRO_jsp.getRuTokenCertificatesList();
    }

    /** Функция для получения списка всех загруженных сертификатов
     * @return список всех загруженных сертификатов
     */
    public List<CryptoPROCertificateModel> getCertificates()
    {
        return certificates;
    }

    /** Функция для получения сертификата из загруженного хранилища по его серийному номеру
     * @param serialNumber серийный номер сертификата (указывается на картинке)
     * @return модель сертификата
     */
    public CryptoPROCertificateModel getCertificateBySerialNumber(String serialNumber)
    {
        // если пусто
        if(certificates == null)
        {
            return new CryptoPROCertificateModel();
        }

        // перебираем все сертификаты
        for (CryptoPROCertificateModel certificate : certificates)
        {
            if (certificate.getCertificateSerialNumber().equals(serialNumber))
            {
                return certificate;
            }
        }

        // если ничего не нашлось
        return new CryptoPROCertificateModel();
    }
}
