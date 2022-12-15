package com.webapi.application.services.cryptopro.jsp;

import java.util.List;

public class Main
{
    public /*static*/ void main(String[] args) throws Exception
    {
        CryptoPRO_JSP cryptoPRO_jsp = new CryptoPRO_JSP();

        cryptoPRO_jsp.loadRuTokenCertificates();
        List<CryptoPROCertificateModel> ruTokenCertificatesList = cryptoPRO_jsp.getRuTokenCertificatesList();  // получаем список сертификатов РуТокен

        String password = "12345678";
        String fileName = "C:\\Users\\ASUS\\Downloads\\googleplaystore.csv";
        cryptoPRO_jsp.createSign(fileName, ruTokenCertificatesList.get(4), password, true);
    }
}
