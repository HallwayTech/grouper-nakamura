package edu.nyu.grouper.xmpp;

import java.net.MalformedURLException;
import java.net.URL;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject;
import edu.nyu.grouper.xmpp.exceptions.GroupModificationException;

public class HttpNakamuraGroupAdapter implements NakamuraGroupAdapter {
	
	private Log log = GrouperClientUtils.retrieveLog(HttpNakamuraGroupAdapter.class);
	
	private static String PROP_KEY_NAKAMURA_URL = "grouperClient.xmpp.nakamura.url";
	private static String PROP_KEY_NAKAMURA_USERNAME = "grouperClient.xmpp.nakamura.username";
	private static String PROP_KEY_NAKAMURA_PASSWORD = "grouperClient.xmpp.nakamura.password";
	
	private URL url;
	private String username;
	private String password;

	public HttpNakamuraGroupAdapter() {
		this.configure();
	}
	
	/**
	 * Read in config values from $GROUPER_CLIENT_HOME/conf/grouper.client.properties
	 * @throws MalformedURLException 
	 */
	private void configure() {
		try {
			url = new URL(GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_URL, true));
			username = GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_USERNAME, true);
			password = GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_PASSWORD, true);
		}
		catch (MalformedURLException mfe){
			log.error("Could not parse the value of " + PROP_KEY_NAKAMURA_URL + " : " 
					+ GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_URL, false) );
			throw new RuntimeException(mfe.toString());
		}
	}

	public void createGroup(String groupId) throws GroupModificationException {
		// http://localhost:8080/system/userManager/group.create.json 

	}

	public void deleteGroup(String groupId) throws GroupModificationException {
		// http://localhost:8080/system/userManager/group.delete.json

	}

	public void addMembership(String groupId, GrouperClientXmppSubject subjectId)
			throws GroupModificationException {
		// http://localhost:8080/system/userManager/group/groupId.update.json :member=subjectId

	}

	public void deleteMembership(String groupId, GrouperClientXmppSubject subjectId)
			throws GroupModificationException {
		// http://localhost:8080/system/userManager/group/groupId.update.json :member@Delete=subjectId

	}

}
