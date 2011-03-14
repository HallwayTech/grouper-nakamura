package edu.nyu.grouper.xmpp;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PostMethod;
import edu.nyu.grouper.xmpp.api.InitialGroupPropertiesProvider;

/**
 * Provide a set of static properties for new sakai groups.
 * @author froese
 */
public class StaticInitialGroupPropertiesProvider implements InitialGroupPropertiesProvider {

	public void addProperties(String groupId, String groupExtension,
			PostMethod method) {
		method.addParameter("group-joinable", "no");
		method.addParameter("group-visible", "members-only");
		method.addParameter("sakai:pages-visible", "members-only");
	}
}
