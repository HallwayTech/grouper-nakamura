package edu.nyu.grouper.xmpp.api;

import java.util.HashMap;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PostMethod;

/**
 * Provide a NakamuraGroupAdapter with a list of initial properties for a Group.
 * @author froese
 */
public interface InitialGroupPropertiesProvider {

	public void addProperties(String groupId, String groupExtension, PostMethod method);

}
