package edu.nyu.grouper.xmpp;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject;
import edu.internet2.middleware.subject.Subject;

import edu.nyu.grouper.api.NakamuraGroupAdapter;
import edu.nyu.grouper.esb.HttpNakamuraGroupAdapter;
import edu.nyu.grouper.util.BaseNakamuraGroupIdAdapter;
import edu.nyu.grouper.util.StaticInitialGroupPropertiesProvider;
import edu.nyu.grouper.exceptions.GroupModificationException;

/**
 * 
 * Respond to Grouper change log events by creating, updating, 
 * or deleting group information in nakamura via a {@link NakamuraGroupAdapter}.
 */
public class NakamuraGroupXmppHandler implements GrouperClientXmppHandler {
	
	private Log log = GrouperClientUtils.retrieveLog(NakamuraGroupXmppHandler.class);
	
	private static String PROP_KEY_NAKAMURA_URL = "grouperClient.xmpp.nakamura.url";
	private static String PROP_KEY_NAKAMURA_USERNAME = "grouperClient.xmpp.nakamura.username";
	private static String PROP_KEY_NAKAMURA_PASSWORD = "grouperClient.xmpp.nakamura.password";
	private static String PROP_KEY_NAKAMURA_BASESTEM = "grouperClient.xmpp.nakamura.basestem";
	
	private GrouperSession grouperSession;
	private Subject grouperSystemSubject;
	
	private HttpNakamuraGroupAdapter groupAdapter;
	
	public NakamuraGroupXmppHandler(){
		this.groupAdapter = new HttpNakamuraGroupAdapter();
		groupAdapter.setUrl(GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_URL, true));
		groupAdapter.setUsername(GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_USERNAME, true));
		groupAdapter.setPassword(GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_PASSWORD, true));
		groupAdapter.setInitialPropertiesProvider(new StaticInitialGroupPropertiesProvider());
		groupAdapter.setGroupIdAdapter(
				new BaseNakamuraGroupIdAdapter(GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_BASESTEM, true)));
		
		grouperSystemSubject = SubjectFinder.findRootSubject();
	}

	/**
	 * @param grouperClientXmppJob the job responsible for this handler.
	 * @param groupName the name attribute from grouper.
	 * @param groupExtension the stem:groupName from grouper.
	 * @param newSubjectList the list of subjects in the group.
	 * @param previousSubjectList 
	 * @param changeSubject the subjectId that is the target of this action
	 * @param action The type of action we're responding to
	 */
	public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob,
			String groupName, String groupExtension,
			List<GrouperClientXmppSubject> newSubjectList,
			List<GrouperClientXmppSubject> previousSubjectList,
			GrouperClientXmppSubject changeSubject, String action) {
		
		try {

			if (log.isDebugEnabled()){
				log.debug("action=" + action + " groupName=" + groupName + " groupExtension=" + groupExtension + " changeSubject=" + changeSubject.getName());
			}

			if (GrouperClientUtils.equals(action, "MEMBERSHIP_ADD")) {
			    groupAdapter.addMembership(groupName, groupExtension, changeSubject.getSubjectId());
			} 
			else if (GrouperClientUtils.equals(action, "MEMBERSHIP_DELETE")) {
				groupAdapter.deleteMembership(groupName, groupExtension, changeSubject.getSubjectId());
			} 
			else if (GrouperClientUtils.equals(action, "GROUP_ADD")) {
				Group group = GroupFinder.findByName(getGrouperSession(), groupExtension, false);
				if (group != null){
					groupAdapter.createGroup(group);
				}
			}
			else if (GrouperClientUtils.equals(action, "GROUP_DELETE")) {
				Group group = GroupFinder.findByName(getGrouperSession(), groupExtension, false);
				if (group != null){
					groupAdapter.deleteGroup(groupName, groupExtension);
				}
			}
			else {
				throw new RuntimeException("Not expecting action: '" + action + "'");
			}
		}
		catch (GroupModificationException gme){
			log.error(gme.toString());
		}
	}

	/**
	 * This method receives the whole group membership. Useful for full refresh jobs.
	 * @param grouperClientXmppJob
	 * @param groupName
	 * @param groupExtension
	 * @param newSubjectList
	 */
	public void handleAll(GrouperClientXmppJob grouperClientXmppJob,
			String groupName, String groupExtension,
			List<GrouperClientXmppSubject> newSubjectList) {
		// TODO Auto-generated method stub
	}
	
	private GrouperSession getGrouperSession(){
		if ( grouperSession == null ) {
			try {
				grouperSession = GrouperSession.start(grouperSystemSubject, false);
				if (log.isDebugEnabled()){
					log.debug("started session: " + grouperSession);
				}
			}
			catch (SessionException se) {
				throw new GrouperException( "error starting session: " + se.getMessage(), se );
			}
		}
		return grouperSession;
	}
}