package com.webapi.application.services.cryptopro.jsp;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CryptoPROCertificateModel
{
    private String alias;
    private X509Certificate x509Certificate;
    private Map<String, String> params;
    private KeyStore keyStore;  // хранилище сертификатов
    private PrivateKey privateKey;
    private String certificateName;
    private String owner;
    private String surname;
    private String nameAndPatronymic;
    private String position;    // должность
    private String department; // отдел
    private String certificateSerialNumber;     // серийный номер сертификата (отображается на картинке)
    private Date validFrom;     // дата начала действия сертификата
    private Date validTo;       // дата окончания действия сертификата
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
        this.setValidFrom(x509Certificate.getNotBefore());
        this.setValidTo(x509Certificate.getNotAfter());
        System.out.println(params);

        for(String key : params.keySet())
        {
            switch (key)
            {
                case "CN":
                {
                    this.certificateName = params.get(key);
                    break;
                }
                case "O":
                {
                    this.owner = params.get(key);
                    break;
                }
                case "SN":
                {
                    this.surname = params.get(key);
                    break;
                }
                case "G":
                {
                    this.nameAndPatronymic = params.get(key);
                    break;
                }
                case "T":
                {
                    this.department = params.get(key);
                    break;
                }
            }
        }
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

    public String getCertificateSerialNumber()
    {
        return certificateSerialNumber;
    }

    public void setCertificateSerialNumber(String certificateSerialNumber)
    {
        this.certificateSerialNumber = certificateSerialNumber;
    }

    public Date getValidFrom()
    {
        return validFrom;
    }

    public void setValidFrom(Date validFrom)
    {
        this.validFrom = validFrom;
    }

    public Date getValidTo()
    {
        return validTo;
    }

    public void setValidTo(Date validTo)
    {
        this.validTo = validTo;
    }

    @Override
    public String toString()
    {
        return "CertificateModel{" + "alias='" + alias + '\'' + ", x509Certificate=" + x509Certificate + ", params=" + params + ", keyStore=" + keyStore + ", privateKey=" + privateKey + ", certificateName='" + certificateName + '\'' + ", owner='" + owner + '\'' + ", surname='" + surname + '\'' + ", nameAndPatronymic='" + nameAndPatronymic + '\'' + ", position='" + position + '\'' + ", department='" + department + '\'' + '}';
    }

    public String getCertificateName()
    {
        return certificateName;
    }

    public String getOwner()
    {
        return owner;
    }

    public String getSurname()
    {
        return surname;
    }

    public String getNameAndPatronymic()
    {
        return nameAndPatronymic;
    }

    public String getPosition()
    {
        return position;
    }

    public String getDepartment()
    {
        return department;
    }
}
