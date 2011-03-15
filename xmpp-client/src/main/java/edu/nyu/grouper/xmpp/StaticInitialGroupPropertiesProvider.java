package edu.nyu.grouper.xmpp;

import org.apache.commons.httpclient.methods.PostMethod;
import edu.nyu.grouper.xmpp.api.InitialGroupPropertiesProvider;

/**
 * Provide a set of static properties for new sakai groups.
 * @author froese
 */
public class StaticInitialGroupPropertiesProvider implements InitialGroupPropertiesProvider {

	public void addProperties(String groupId, String groupExtension,
			PostMethod method) {
		
		method.addParameter("sakai:group-id", groupExtension);
		method.addParameter("sakai:group-title", groupExtension);
		method.addParameter("sakai:group-description", "Created by Grouper");
		method.addParameter("sakai:pages-template", "/var/templates/site/defaultgroup");
		
		// Authorizations
		method.addParameter("group-joinable", "no");
		method.addParameter("group-visible", "members-only");
		method.addParameter("sakai:pages-visible", "members-only");
	}
}
