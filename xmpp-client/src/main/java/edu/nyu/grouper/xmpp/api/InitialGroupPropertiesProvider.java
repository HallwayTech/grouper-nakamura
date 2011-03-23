package edu.nyu.grouper.xmpp.api;

import org.apache.commons.httpclient.methods.PostMethod;

import edu.internet2.middleware.grouper.Group;

/**
 * Provide a NakamuraGroupAdapter with a list of initial properties for a Group.
 * @author Erik Froese
 */
public interface InitialGroupPropertiesProvider {

	public void addProperties(Group group, PostMethod method);

}
