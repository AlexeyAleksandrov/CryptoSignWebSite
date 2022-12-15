package com.webapi.application.services.cryptopro.jsp;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CyptoProSignService
{
    private CryptoPRO_JSP cryptoPRO_jsp;    // обработчик Крипто ПРО
    private List<CryptoPROCertificateModel> certificates;

    public CyptoProSignService(CryptoPRO_JSP cryptoPRO_jsp)
    {
        this.cryptoPRO_jsp = cryptoPRO_jsp;
        this.cryptoPRO_jsp.loadRuTokenCertificates();
        certificates = this.cryptoPRO_jsp.getRuTokenCertificatesList();
    }

    public List<CryptoPROCertificateModel> getCertificates()
    {
        return certificates;
    }

    public CryptoPROCertificateModel getCertificateBySerial
}
