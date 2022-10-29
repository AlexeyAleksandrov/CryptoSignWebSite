package com.webapi.application.services.cryptopro.jsp;

import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        CryptoPRO_JSP cryptoPRO_jsp = new CryptoPRO_JSP();

        List<CertificateModel> ruTokenCertificatesList = cryptoPRO_jsp.getRuTokenCertificates();  // получаем список сертификатов РуТокен

        String password = "12345678";
        String fileName = "C:\\Users\\ASUS\\Downloads\\Расписание пар Ксюша.pdf";
        cryptoPRO_jsp.createSign(fileName, ruTokenCertificatesList.get(4), password, true);
    }
}
