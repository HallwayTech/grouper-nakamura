package edu.nyu.grouper.util;

import org.apache.commons.httpclient.methods.PostMethod;

import edu.internet2.middleware.grouper.Group;
import edu.nyu.grouper.util.api.InitialGroupPropertiesProvider;

/**
 * Set HTTP POST params that become properties on the group in SakaiOAE
 * @author froese
 */
public class StaticInitialGroupPropertiesProvider implements InitialGroupPropertiesProvider {

	public void addProperties(Group group, String nakamuraGroupId, PostMethod method) {

		// Basic info
		method.addParameter("sakai:group-id", nakamuraGroupId);
		method.addParameter("sakai:group-description", group.getParentStem().getDescription());
		method.addParameter(":sakai:pages-template", "/var/templates/site/defaultgroup");

		// Authorizations
		method.addParameter(":sakai:manager", "admin");

		// Managers groups
		if (nakamuraGroupId.endsWith("-managers")) {
			method.addParameter("sakai:bare", "true");
			method.addParameter("sakai:managed-group", 
					nakamuraGroupId.substring(0, nakamuraGroupId.indexOf("-managers")));
		}
		else {
			method.addParameter("sakai:group-title", group.getParentStem().getExtension());
			method.addParameter("sakai:pages-visible", "members-only");
			method.addParameter("group-joinable", "no");
			method.addParameter("group-visible", "members-only");
		}

		// Grouper
		method.addParameter("grouper:name", group.getName());
		method.addParameter("grouper:uuid", group.getUuid());
	}
}