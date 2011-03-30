package edu.nyu.grouper.util;

import java.net.URL;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class GrouperHttpUtil {
	/**
	 * Construct an {@link HttpClient} which is configured to authenticate to Nakamura.
	 * @return the configured client.
	 */
	public static HttpClient getHttpClient(URL url, String username, String password){
		HttpClient client = new HttpClient();

		DefaultHttpParams.getDefaultParams().setParameter(
                HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

		HttpState state = client.getState();
		state.setCredentials(
				new AuthScope(url.getHost(), getPort(url)),
				new UsernamePasswordCredentials(username, password));
		client.getParams().setAuthenticationPreemptive(true);
		return client;
	}

	/**
	 * If you don't specify a port when creating a {@link URL} {@link URL#getPort()} will return -1.
	 * This function uses the default HTTP/s ports  
	 * @return the port for this.url. 80 or 433 if not specified.
	 */
	private static int getPort(URL url){
		int port = url.getPort();
		if (port == -1){
			if (url.getProtocol().equals("http")){
				port = 80;
			}
			else if(url.getProtocol().equals("https")){
				port = 443;
			}
		}
		return port;
	}
}
