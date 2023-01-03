package com.webapi.application.services.cryptopro.jsp;

import CMS_samples.CMStools;
import com.objsys.asn1j.runtime.*;
import org.springframework.stereotype.Component;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Name;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.reprov.RevCheck;

import java.io.IOException;
import java.security.*;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Component
public class CryptoPRO_JSP
{
    /**
     * КриптоПРО JSP провайдер
     */
    private final Provider prov;
    /**
     * Внутренний список сертификатов РуТокен
     */
    private List<CryptoPROCertificateModel> ruTokenCertificatesList; // TODO: Продумать, как быть с паролями от токенов

    /** Получение списка загруженных сертификатов РуТокен
     * @return список загруженных ранее сертификатов РуТокен
     */
    public List<CryptoPROCertificateModel> getRuTokenCertificatesList()
    {
        return ruTokenCertificatesList;
    }

    /**
     * Обновить/загрузить список доступных в данный момент сертификатов РуТокен
     */
    public void loadRuTokenCertificates()
    {
        ruTokenCertificatesList = readRuTokenCertificates();
    }

    /**
     * Конструктор. Выполняется настройка подключения КриптПРО
     */
    public CryptoPRO_JSP()
    {
        // КриптоПРО провайдер
        prov = new JCP();
        try {
            System.out.println("CryptoPro start initialization");
            System.setProperty("file.encoding", "UTF-8");
            //            Security.addProvider(new JCP());
            Security.addProvider(prov);
            Security.addProvider(new JCSP()); // провайдер JCSP
            Security.addProvider(new RevCheck());// провайдер проверки сертификатов JCPRevCheck
            //(revocation-провайдер)
            Security.addProvider(new CryptoProvider());// провайдер шифрования JCryptoSystem.out.println("CryptoPro initialized");

        } catch (Exception e) {
            System.out.println("ErroeException in initializing crypto pro : "+ e);
        } catch (Error er) {
            System.out.println("Error in initializing crypto pro : "+ er);
        }

//        ruTokenCertificatesList = getRuTokenCertificates(prov, "12345678".toCharArray());  // получаем список сертификатов РуТокен
    }

    /** Чтение всех сертификатов со всех подключенных токенов РуТокен
     * @return список сертификатов
     */
    private List<CryptoPROCertificateModel> readRuTokenCertificates()
    {
        int k = 0;  // счётчик кол-ва попыток считывания сертификатов

        List<CryptoPROCertificateModel> certificatesList = new ArrayList<>();     // список сертификатов

        while (k < 1000)   // Обычно с 1 раза не срабатывает. Это проблема КриптоПРО, обычно на 2-3 раз всё нормально, но на всякий случай доступно до 1000 попыток
        {
            try
            {
                System.out.println("Попытка №" + (k+1));

                final String KEYSTORE_TYPE = "RutokenStore";    // тип хранилища
                final Provider KEYSTORE_PROVIDER = prov;        // провайдер

                final KeyStore hdImageStore = KeyStore.getInstance(KEYSTORE_TYPE, KEYSTORE_PROVIDER);   // получаем хранилище сертификатов
                hdImageStore.load(null, null);      // загружаем список сертификатов
                System.out.println("Loading...");       // процесс долгий, обычно секунд 10 на каждый сертификат уходит

                Enumeration<String> aliases = hdImageStore.aliases();       // получаем список ID загруженных сертификатов

                while (aliases.hasMoreElements())   // перебор всех ID
                {
                    String alias = aliases.nextElement();
                    Certificate cert = hdImageStore.getCertificate(alias);      // получаем сертификат из хранилища по его ID
                    if (cert == null) {
                        continue;
                    }
                    if (!(cert instanceof X509Certificate)) {
                        continue;
                    }
                    X509Certificate curCert = (X509Certificate) cert;       // преобразовываем к сертификату для создания подписи

                    // На всякий случай. не удалять!!!
                    // === способ 1 - рабочий ===
//                    KeyStore.ProtectionParameter protectedParam = new KeyStore.PasswordProtection("12345678".toCharArray());
//                    JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) hdImageStore.getEntry(alias, protectedParam);
//
//                    PrivateKey privateKey = entry.getPrivateKey();
                    // ==== конец 1-го способа ===

                    // === способ 2 - вроде работает ===
//                    PrivateKey privateKey = (PrivateKey) hdImageStore.getKey(alias, password);
                    // === конец 2 способа ===

                    // помещаем полученный сертификат в модель сертификата для удобства дальнейшей работы
                    CryptoPROCertificateModel cryptoPROCertificateModel = new CryptoPROCertificateModel();     // создаем модель сертификата
                    cryptoPROCertificateModel.setAlias(alias);
                    cryptoPROCertificateModel.setX509Certificate(curCert);
                    cryptoPROCertificateModel.setKeyStore(hdImageStore);
//                    certificateModel.setPrivateKey(privateKey); // закрытый ключ лучше не хранить, пользователь сам его будет вводить
                    cryptoPROCertificateModel.setCertificateSerialNumber(curCert.getSerialNumber().toString(16).toUpperCase());     // серийный номер сертификата (отображается на картинке)

                    certificatesList.add(cryptoPROCertificateModel);  // добавляем сертификат в список
                }

                System.out.println("Load complete!");
                break;
            }
            catch (CertificateException | NoSuchAlgorithmException | RuntimeException | IOException | KeyStoreException e)
            {
                if(e.getMessage().equals("RutokenStore not found"))
                {
                    System.out.println("РуТокен не вставлен!");
                    break;
                }
                else if(e.getMessage().contains("Card has been disconnected"))
                {
                    System.out.println("Не удалось считать данные с РуТокен");
                }
                else
                {
                    e.printStackTrace();
                }
            }
            k++;
        }

        return certificatesList;
    }

    /** Получение списка локальных (установленных) сертификатов
     * @return список сертификатов
     */
    private List<CryptoPROCertificateModel> readLocalCertificates()
    {
        List<CryptoPROCertificateModel> certificatesList = new ArrayList<>();     // список сертификатов

        try
        {
            KeyStore ks = KeyStore.getInstance("REGISTRY", "JCSP");

            ks.load(null, null);
            Enumeration<String> aliases = ks.aliases();

            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate cert = ks.getCertificate(alias);
                //                Certificate cert = ks.getCertificate(aliases.nextElement());
                if (cert == null) {
                    continue;
                }
                if (!(cert instanceof X509Certificate)) {
                    continue;
                }
                X509Certificate curCert = (X509Certificate) cert;

                CryptoPROCertificateModel cryptoPROCertificateModel = new CryptoPROCertificateModel();     // создаем модель сертификата
                cryptoPROCertificateModel.setAlias(alias);
                cryptoPROCertificateModel.setX509Certificate(curCert);
                cryptoPROCertificateModel.setKeyStore(ks);
                cryptoPROCertificateModel.setCertificateSerialNumber(curCert.getSerialNumber().toString(16).toUpperCase());     // серийный номер сертификата (отображается на картинке)

                certificatesList.add(cryptoPROCertificateModel);  // добавляем сертификат в список
            }
        } catch (KeyStoreException e) {
            System.err.println("Error: "+ e);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }

        return certificatesList;
    }

    /** Создание электронной подписи к файлу
     * @param fileName полный путь к файлу, который будет подписываться
     * @param cryptoPROCertificateModel сертификат, которым выполняется подпись
     * @param tokenPassword пароль от РуТокен
     * @param detached отсоединённая подпись (true - отсоединённая, false - присоединённая)
     * @throws Exception ошибки подписи
     */
    public void createSign(String fileName, CryptoPROCertificateModel cryptoPROCertificateModel, String tokenPassword, boolean detached) throws Exception
    {
        // TODO: заменить Exception на return bool - корректно или некорректно выполнена подпись
        // TODO: добавить Enum с результатом выполнения в качестве параметра функции - записывать туда значения ошибок
        // TODO: сделать проверку на существование файла
        final byte[] data = Array.readFile(fileName);   // читаем подписываемый файл как бинарный
        String alias = cryptoPROCertificateModel.getAlias();    // получаем ID ключа
        KeyStore keyStore = cryptoPROCertificateModel.getKeyStore();    // получаем хранилище сертификатов
        char[] password = tokenPassword.toCharArray();      // конвертируем пароль в массив символов
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);      // пытаемся получить закрытый ключ используя пароль
        // TODO: сделать try для пароля и выдавать ошибку
        // TODO: сделать проверку на правильный алгоритм (try/catch)
        Array.writeFile( fileName + ".sig", CMSSignEx(data, privateKey, cryptoPROCertificateModel.getX509Certificate(), detached,
                JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_SIGN_2012_256_OID, JCP.GOST_SIGN_2012_256_NAME, JCP.PROVIDER_NAME) );
    }

    /** Функция из документации - sign CMS, как работает - не знаю :)
     * @param data data
     * @param key key
     * @param cert cert
     * @param detached detached signature
     * @param digestOid digest algorithm OID
     * @param signOid signature algorithm OID
     * @param signAlg signature algorithm name
     * @param providerName provider name
     * @throws Exception e
     * @since 2.0
     */
    private byte[] CMSSignEx(byte[] data, PrivateKey key,
                                   Certificate cert, boolean detached, String digestOid,
                                   String signOid, String signAlg, String providerName)
    throws Exception {

        // sign
        final Signature signature = Signature.getInstance(signAlg, providerName);
        signature.initSign(key);
        signature.update(data);

        final byte[] sign = signature.sign();

        // create cms format
        return createCMSEx(data, sign, cert, detached, digestOid, signOid);
    }

    /**
     * createCMS
     *
     * @param buffer buffer
     * @param sign sign
     * @param cert cert
     * @param detached detached signature
     * @param digestOid digest algorithm OID (to append to CMS)
     * @param signOid signature algorithm OID (to append to CMS)
     * @return byte[]
     * @throws Exception e
     * @since 2.0
     */
    private byte[] createCMSEx(byte[] buffer, byte[] sign,
                                     Certificate cert, boolean detached, String digestOid,
                                     String signOid) throws Exception {

        final ContentInfo all = new ContentInfo();
        all.contentType = new Asn1ObjectIdentifier(
                new OID(CMStools.STR_CMS_OID_SIGNED).value);

        final SignedData cms = new SignedData();
        all.content = cms;
        cms.version = new CMSVersion(1);

        // digest
        cms.digestAlgorithms = new DigestAlgorithmIdentifiers(1);
        final DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier(
                new OID(digestOid).value);

        a.parameters = new Asn1Null();
        cms.digestAlgorithms.elements[0] = a;

        if (detached) {
            cms.encapContentInfo = new EncapsulatedContentInfo(
                    new Asn1ObjectIdentifier(
                            new OID(CMStools.STR_CMS_OID_DATA).value), null);
        } // if
        else {
            cms.encapContentInfo =
                    new EncapsulatedContentInfo(new Asn1ObjectIdentifier(
                            new OID(CMStools.STR_CMS_OID_DATA).value),
                            new Asn1OctetString(buffer));
        } // else

        // certificate
        cms.certificates = new CertificateSet(1);
        final ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate certificate =
                new ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate();
        final Asn1BerDecodeBuffer decodeBuffer =
                new Asn1BerDecodeBuffer(cert.getEncoded());
        certificate.decode(decodeBuffer);

        cms.certificates.elements = new CertificateChoices[1];
        cms.certificates.elements[0] = new CertificateChoices();
        cms.certificates.elements[0].set_certificate(certificate);

        // signer info
        cms.signerInfos = new SignerInfos(1);
        cms.signerInfos.elements[0] = new SignerInfo();
        cms.signerInfos.elements[0].version = new CMSVersion(1);
        cms.signerInfos.elements[0].sid = new SignerIdentifier();

        final byte[] encodedName = ((X509Certificate) cert)
                .getIssuerX500Principal().getEncoded();
        final Asn1BerDecodeBuffer nameBuf = new Asn1BerDecodeBuffer(encodedName);
        final Name name = new Name();
        name.decode(nameBuf);

        final CertificateSerialNumber num = new CertificateSerialNumber(
                ((X509Certificate) cert).getSerialNumber());
        cms.signerInfos.elements[0].sid.set_issuerAndSerialNumber(
                new IssuerAndSerialNumber(name, num));
        cms.signerInfos.elements[0].digestAlgorithm =
                new DigestAlgorithmIdentifier(new OID(digestOid).value);
        cms.signerInfos.elements[0].digestAlgorithm.parameters = new Asn1Null();
        cms.signerInfos.elements[0].signatureAlgorithm =
                new SignatureAlgorithmIdentifier(new OID(signOid).value);
        cms.signerInfos.elements[0].signatureAlgorithm.parameters = new Asn1Null();
        cms.signerInfos.elements[0].signature = new SignatureValue(sign);

        // encode
        final Asn1BerEncodeBuffer asnBuf = new Asn1BerEncodeBuffer();
        all.encode(asnBuf, true);
        return asnBuf.getMsgCopy();
    }

    public Provider getProv()
    {
        return prov;
    }
}
