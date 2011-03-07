package edu.nyu.grouper.xmpp;

import java.util.List;

import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject;

/**
 * 
 * Respond to Grouper Changelog events by creating, updating, 
 * or deleting group information in nakamura.
 */
public class AuthorizableHandler implements GrouperClientXmppHandler {

	public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob,
			String groupName, String groupExtension,
			List<GrouperClientXmppSubject> newSubjectList,
			List<GrouperClientXmppSubject> previousSubjectList,
			GrouperClientXmppSubject changeSubject, String action) {
		// TODO Auto-generated method stub

	}

	public void handleAll(GrouperClientXmppJob grouperClientXmppJob,
			String groupName, String groupExtension,
			List<GrouperClientXmppSubject> newSubjectList) {
		// TODO Auto-generated method stub

	}

}
