package edu.nyu.grouper.util.api;

import org.apache.commons.httpclient.methods.PostMethod;

import edu.internet2.middleware.grouper.Group;

/**
 * Add HTTP parameters to a request that will create a new group
 * @author Erik Froese
 */
public interface InitialGroupPropertiesProvider {

	/**
	 * Add HTTP parameters to a request that will create a new group
	 * @param group The Grouper group we're creating in sakai.
	 * @param nakamuraGroupId The group's sakai:group-id in sakai3
	 * @param method the HTTP POST that will create this group.
	 */
	public void addProperties(Group group, String nakamuraGroupId, PostMethod method);
}