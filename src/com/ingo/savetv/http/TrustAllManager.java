package com.ingo.savetv.http;

import javax.net.ssl.X509TrustManager;

public class TrustAllManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
			String authType) throws java.security.cert.CertificateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
			String authType) throws java.security.cert.CertificateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return null;
	}

}
