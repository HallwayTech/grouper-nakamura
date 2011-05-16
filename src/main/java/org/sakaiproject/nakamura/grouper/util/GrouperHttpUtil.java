/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.grouper.util;

import java.net.URL;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;

public class GrouperHttpUtil {
	/**
	 * Construct an {@link HttpClient} which is configured to authenticate to Nakamura.
	 * @return the configured client.
	 */
	public static HttpClient getHttpClient(GrouperConfiguration grouperConfiguration){
		HttpClient client = new HttpClient();

		DefaultHttpParams.getDefaultParams().setParameter(
                HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

		HttpState state = client.getState();
		state.setCredentials(
				new AuthScope(grouperConfiguration.getUrl().getHost(), getPort(grouperConfiguration.getUrl())),
				new UsernamePasswordCredentials(grouperConfiguration.getUsername(), grouperConfiguration.getPassword()));
		client.getParams().setAuthenticationPreemptive(true);
		client.getParams().setSoTimeout(grouperConfiguration.getHttpTimeout());
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
