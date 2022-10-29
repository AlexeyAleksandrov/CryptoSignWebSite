package ru.CryptoPro.ssl.tomcat.jsse;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;

public class JCPSSLContext implements 
	org.apache.tomcat.util.net.SSLContext {
	
	private javax.net.ssl.SSLContext context;

	JCPSSLContext(String protocol) throws NoSuchAlgorithmException {
		this.context = javax.net.ssl.SSLContext.getInstance(protocol);
	}

	public void init(KeyManager[] kms, TrustManager[] tms, SecureRandom sr)
	    throws KeyManagementException {
		this.context.init(kms, tms, sr);
	}

	public void destroy() {}

	public SSLSessionContext getServerSessionContext() {
		return this.context.getServerSessionContext();
	}

	public SSLEngine createSSLEngine() {
		return this.context.createSSLEngine();
	}

	public SSLServerSocketFactory getServerSocketFactory() {
		return this.context.getServerSocketFactory();
	}

	public SSLParameters getSupportedSSLParameters() {
		return this.context.getSupportedSSLParameters();
	}
	
}
