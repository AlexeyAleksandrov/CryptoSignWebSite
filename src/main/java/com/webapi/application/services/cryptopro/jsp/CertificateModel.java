package com.webapi.application.services.cryptopro.jsp;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class CertificateModel
{
    private String alias;
    private X509Certificate x509Certificate;
    private Map<String, String> params;
    private KeyStore keyStore;  // хранилище сертификатов
    private PrivateKey privateKey;
//    TODO: Доделать поля, такие как CN и т.п.

    public static Map<String, String> getParamsByX509Certificate(X509Certificate x509Certificate)
    {
        String[] tempParamsList =  x509Certificate.getSubjectX500Principal().getName().split(",");  // разбиваем параметры через запятую
        Map<String, String> params = new HashMap<>();   // список параметров в формате ключ значение

        for (int j = 0; j < tempParamsList.length; j++)
        {
            String line = tempParamsList[j];    // получаем значение пары параметров
            String[] linePair = line.split("=");    // разбиваем строку на значение слева и справа

            String key = linePair[0];   // ключ
            String value = linePair[1]; // значение

            params.put(key, value);   // записываем в map
        }

        return params;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public X509Certificate getX509Certificate()
    {
        return x509Certificate;
    }

    public void setX509Certificate(X509Certificate x509Certificate)
    {
        this.x509Certificate = x509Certificate;
        this.params = getParamsByX509Certificate(x509Certificate);
    }

    public Map<String, String> getParams()
    {
        return params;
    }

    public KeyStore getKeyStore()
    {
        return keyStore;
    }

    public void setKeyStore(KeyStore keyStore)
    {
        this.keyStore = keyStore;
    }

    public PrivateKey getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey)
    {
        this.privateKey = privateKey;
    }
}
