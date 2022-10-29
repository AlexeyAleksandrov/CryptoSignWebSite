package ru.CryptoPro.ssl.tomcat.jsse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.juli.logging.Log;

import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLUtil;

public abstract class JCPSSLUtilBase implements SSLUtil {

	protected final SSLHostConfigCertificate certificate;
	private final String[] enabledProtocols;
	private final String[] enabledCiphers;
	
	protected JCPSSLUtilBase(SSLHostConfigCertificate certificate) {
		
		this.certificate = certificate;
	    SSLHostConfig sslHostConfig = certificate.getSSLHostConfig();

	    Set<String> configuredProtocols = sslHostConfig.getProtocols();
	    Set<String> implementedProtocols = getImplementedProtocols();
	    
	    List<String> enabledProtocols = getEnabled("protocols", getLog(),
	    	true, configuredProtocols, implementedProtocols);
	    
	    this.enabledProtocols = enabledProtocols.toArray(
	    	new String[enabledProtocols.size()]);
	    
	    List<String> configuredCiphers = getJsseCipherNames(); // Используем собственный список
	    Set<String> implementedCiphers = getImplementedCiphers();
	    
	    List<String> enabledCiphers = getEnabled("ciphers", getLog(), 
	    	false, configuredCiphers, implementedCiphers);
	    
	    this.enabledCiphers = enabledCiphers.toArray(
	    	new String[enabledCiphers.size()]);
		
	}
	
	static List<String> getJsseCipherNames() {
		List<String> cipherSuites = new ArrayList<String>();
		cipherSuites.add("TLS_CIPHER_2012");
		cipherSuites.add("TLS_CIPHER_2001");
		cipherSuites.add("SSL3_CK_GVO_KB2");
		cipherSuites.add("SSL3_CK_GVO");
		return cipherSuites;
	}
	
	static <T> List<T> getEnabled(String name, Log log, boolean warnOnSkip, 
	Collection<T> configured, Collection<T> implemented) {
		
	    List<T> enabled = new ArrayList();
	    if (implemented.size() == 0)
	    {
	      enabled.addAll(configured);
	    }
	    else
	    {
	    	
	      enabled.addAll(configured);
	      enabled.retainAll(implemented);
	      
	      if (enabled.isEmpty()) {
	        throw new IllegalArgumentException("None supported: " + name);
	      }
	      if (((log.isDebugEnabled()) || (warnOnSkip)) && 
	        (enabled.size() != configured.size()))
	      {
	        List<T> skipped = new ArrayList();
	        skipped.addAll(configured);
	        skipped.removeAll(enabled);
	        String msg = "Skipped: " + name;
	        if (warnOnSkip) {
	          log.warn(msg);
	        } else {
	          log.debug(msg);
	        }
	      }
	    }
	    return enabled;
	  }
	  
	  public String[] getEnabledProtocols() {
	    return this.enabledProtocols;
	  }
	  
	  public String[] getEnabledCiphers() {
	    return this.enabledCiphers;
	  }
	  
	  protected abstract Set<String> getImplementedProtocols();
	  
	  protected abstract Set<String> getImplementedCiphers();
	  
	  protected abstract Log getLog();

}
