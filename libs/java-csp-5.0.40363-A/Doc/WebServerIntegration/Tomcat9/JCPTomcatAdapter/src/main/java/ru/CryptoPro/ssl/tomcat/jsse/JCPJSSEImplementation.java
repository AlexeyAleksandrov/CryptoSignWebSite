package ru.CryptoPro.ssl.tomcat.jsse;

import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;

import java.security.Security;

public class JCPJSSEImplementation extends JSSEImplementation {
	
	public JCPJSSEImplementation() {
	    super();
        Security.addProvider(new ru.CryptoPro.JCP.JCP());
        Security.addProvider(new ru.CryptoPro.ssl.Provider());
		Security.addProvider(new ru.CryptoPro.Crypto.CryptoProvider());
		Security.addProvider(new ru.CryptoPro.reprov.RevCheck());

	}
	
	public SSLUtil getSSLUtil(SSLHostConfigCertificate certificate) {
	    return new JCPJSSEUtil(certificate); // Собственный класс
	 }

}
