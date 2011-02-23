package org.sakaiproject.nakamura.grouper.api;

import java.net.URL;

public class GrouperWSConfiguration {

	private URL url;
	private String username;
	private String password;

	public GrouperWSConfiguration(URL url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public GrouperWSConfiguration() { }

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
