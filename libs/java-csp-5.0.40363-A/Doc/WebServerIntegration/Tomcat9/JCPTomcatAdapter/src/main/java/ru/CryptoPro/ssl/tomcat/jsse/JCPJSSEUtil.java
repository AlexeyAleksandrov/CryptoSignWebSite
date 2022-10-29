package ru.CryptoPro.ssl.tomcat.jsse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import org.apache.tomcat.util.net.SSLContext;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import ru.CryptoPro.JCP.tools.Platform;
import ru.CryptoPro.ssl.Provider;

public class JCPJSSEUtil extends JCPSSLUtilBase {

	private static final Log log = LogFactory.getLog(JCPJSSEUtil.class);
	private static final Set<String> implementedProtocols;
	private static final Set<String> implementedCiphers;
	private final SSLHostConfig sslHostConfig;
	
	static {
		
		SSLContext context;
		
	    try {

	      context = new JCPSSLContext(Provider.ALGORITHM); // Прямо указан алгоритм из cpSSL
	      context.init(null, null, null);

	    } catch (NoSuchAlgorithmException e) {
	      throw new IllegalArgumentException(e);
	    } catch (KeyManagementException e) {
	    	throw new IllegalArgumentException(e);
	    }
	    
	    SSLServerSocketFactory ssf = context.getServerSocketFactory();
	    implementedProtocols = new HashSet();
	    Exception exception;
	    
	    try {
	    	
	      SSLServerSocket socket = (SSLServerSocket)ssf.createServerSocket();
	      exception = null;
	      
	      try {
	    	  
	        for (String protocol : socket.getEnabledProtocols()) {
	        	
	          String protocolUpper = protocol.toUpperCase(Locale.ENGLISH);
	          if ((!"SSLV2HELLO".equals(protocolUpper)) && (protocolUpper.contains("SSL"))) {
	        	 log.debug("Exclude default protocol: " + protocol);
	          } else {
	            implementedProtocols.add(protocol);
	          }
	          
	        }
	        
	      } catch (Exception e) {
	    	  exception = e;
	    	  throw new RuntimeException(e);
	      } finally {
	    	  
	        if (socket != null) {
	          if (exception != null) {
	        	  
	            try {
	              socket.close();
	            } catch (Exception e) {
	              	exception.addSuppressed(e);
	            }
	            
	          } else {
	            socket.close();
	          }
	          
	        }
	        
	      }
	      
	    } catch (IOException e) {
	    	// ignore
	    }
	    
	    if (implementedProtocols.size() == 0) {
	    	log.warn("No default protocols");
	    }

	    String[] implementedCipherSuiteArray = context.
	        getSupportedSSLParameters().getCipherSuites();
	    
	    if (Platform.isIbm) {
	      implementedCiphers = new HashSet(implementedCipherSuiteArray.length * 2);
	      for (String name : implementedCipherSuiteArray) {
	        implementedCiphers.add(name);
	        if (name.startsWith("SSL")) {
	          implementedCiphers.add("TLS" + name.substring(3));
	        }
	      }
	      
	    } else {
	      implementedCiphers = new HashSet(implementedCipherSuiteArray.length);
	      implementedCiphers.addAll(Arrays.asList(implementedCipherSuiteArray));
	    }
	    
	  }
	
	public JCPJSSEUtil(SSLHostConfigCertificate certificate) {
		super(certificate);
		this.sslHostConfig = certificate.getSSLHostConfig();
	}

	public void configureSessionContext(SSLSessionContext sslSessionContext) {
		sslSessionContext.setSessionCacheSize(this.sslHostConfig.getSessionCacheSize());
	    sslSessionContext.setSessionTimeout(this.sslHostConfig.getSessionTimeout());	
	}

	public SSLContext createSSLContext(List<String> negotiableProtocols) throws Exception {
		return new JCPSSLContext(this.sslHostConfig.getSslProtocol());
	}

	public KeyManager[] getKeyManagers() throws Exception { // Сокращен

	    KeyStore ks = this.certificate.getCertificateKeystore();
	    String keyPass = this.certificate.getCertificateKeystorePassword();
	    
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance(Provider.KEYMANGER_ALG); // Прямо указан алгоритм из cpSSL
	    kmf.init(ks, keyPass.toCharArray());
		
	    return kmf.getKeyManagers();
	    
	}

	public TrustManager[] getTrustManagers() throws Exception { // Сокращен
		
	    KeyStore trustStore = this.sslHostConfig.getTruststore();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(Provider.KEYMANGER_ALG); // Прямо указан алгоритм из cpSSL
        
		tmf.init(trustStore);
        return tmf.getTrustManagers();

	}

	@Override
	protected Set<String> getImplementedCiphers() {
		return implementedCiphers;
	}

	@Override
	protected Set<String> getImplementedProtocols() {
		return implementedProtocols;
	}

	@Override
	protected Log getLog() {
		return log;
	}

}
