package edu.nyu.grouper.osgi.api;

import java.net.URL;

public interface GrouperConfiguration {

	public abstract String getWsVersion();

	public abstract String getUsername();

	public abstract String getPassword();

	public abstract String getBaseStem();
	
	public abstract URL getUrl();

}