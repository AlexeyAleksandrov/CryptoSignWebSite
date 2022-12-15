package com.webapi.application.services.cryptopro.jsp;

import CMS_samples.CMStools;
import com.objsys.asn1j.runtime.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
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
    private final Provider prov;
    private List<CryptoPROCertificateModel> ruTokenCertificatesList; // TODO: Продумать, как быть с паролями от токенов

    public List<CryptoPROCertificateModel> getRuTokenCertificatesList()
    {
        return ruTokenCertificatesList;
    }

    public void loadRuTokenCertificates()
    {
        ruTokenCertificatesList = readRuTokenCertificates();
    }

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

    private List<CryptoPROCertificateModel> readRuTokenCertificates()
    {
        boolean diskonnectedError = true;
        int k = 0;

        List<CryptoPROCertificateModel> certificatesList = new ArrayList<>();     // список сертификатов

        while (diskonnectedError && k < 1000)
        {
            try
            {
                System.out.println("Попытка №" + (k+1));

                final String KEYSTORE_TYPE = "RutokenStore";
                final Provider KEYSTORE_PROVIDER = prov;

                final KeyStore hdImageStore = KeyStore.getInstance(KEYSTORE_TYPE, KEYSTORE_PROVIDER);
                hdImageStore.load(null, null);
                System.out.println("Loading...");

                Enumeration<String> aliases = hdImageStore.aliases();

                while (aliases.hasMoreElements())
                {
                    String alias = aliases.nextElement();
                    Certificate cert = hdImageStore.getCertificate(alias);
                    if (cert == null) {
                        continue;
                    }
                    if (!(cert instanceof X509Certificate)) {
                        continue;
                    }
                    X509Certificate curCert = (X509Certificate) cert;

                    // === способ 1 - рабочий ===
//                    KeyStore.ProtectionParameter protectedParam = new KeyStore.PasswordProtection("12345678".toCharArray());
//                    JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) hdImageStore.getEntry(alias, protectedParam);
//
//                    PrivateKey privateKey = entry.getPrivateKey();
                    // ==== конец 1-го способа ===

                    // === способ 2 - вроде работает ===
//                    PrivateKey privateKey = (PrivateKey) hdImageStore.getKey(alias, password);
                    // === конец 2 способа ===

                    CryptoPROCertificateModel cryptoPROCertificateModel = new CryptoPROCertificateModel();     // создаем модель сертификата
                    cryptoPROCertificateModel.setAlias(alias);
                    cryptoPROCertificateModel.setX509Certificate(curCert);
                    cryptoPROCertificateModel.setKeyStore(hdImageStore);
//                    certificateModel.setPrivateKey(privateKey);
                    cryptoPROCertificateModel.setCertificateSerialNumber(curCert.getSerialNumber().toString(16).toUpperCase());     // серийный номер сертификата (отображается на картинке)

                    certificatesList.add(cryptoPROCertificateModel);  // добавляем сертификат в список
                }
                diskonnectedError = false;

                System.out.println("Load complete!");
            }
            catch (CertificateException | NoSuchAlgorithmException | RuntimeException | IOException | KeyStoreException e)
            {
                if(e.getMessage().equals("RutokenStore not found"))
                {
                    System.out.println("РуТокен не вставлен!");
                    diskonnectedError = false;
                    k = 1000;
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

    public void createSign(String fileName, CryptoPROCertificateModel cryptoPROCertificateModel, String tokenPassword, boolean detached) throws Exception
    {
        final byte[] data = Array.readFile(fileName);
        String alias = cryptoPROCertificateModel.getAlias();
        KeyStore keyStore = cryptoPROCertificateModel.getKeyStore();
        char[] password = tokenPassword.toCharArray();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
        Array.writeFile( fileName + ".sig", CMSSignEx(data, privateKey, cryptoPROCertificateModel.getX509Certificate(), detached,
                JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_SIGN_2012_256_OID, JCP.GOST_SIGN_2012_256_NAME, JCP.PROVIDER_NAME) );
    }

    /**
     * sign CMS
     *
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
