package edu.nyu.grouper.xmpp.api;

import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Provide a NakamuraGroupAdapter with a list of initial properties for a Group.
 * @author Erik Froese
 */
public interface InitialGroupPropertiesProvider {

	public void addProperties(String groupId, String groupExtension, PostMethod method);

}
