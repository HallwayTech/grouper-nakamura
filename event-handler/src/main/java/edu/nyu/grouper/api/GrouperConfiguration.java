package edu.nyu.grouper.api;

import java.net.URL;

public interface GrouperConfiguration {

	public abstract String getWsVersion();

	public abstract String getUsername();

	public abstract String getPassword();

	public abstract String getBaseStem();
	
	public abstract URL getUrl();

	public abstract String getRestWsUrlString();

	public abstract String getSuffix();

	public abstract int getHttpTimeout();

	public abstract String getIgnoredUserId();
}