package edu.nyu.grouper.xmpp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpState;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpStatus;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.auth.AuthScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PostMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject;
import edu.nyu.grouper.xmpp.api.InitialGroupPropertiesProvider;
import edu.nyu.grouper.xmpp.api.NakamuraGroupAdapter;
import edu.nyu.grouper.xmpp.api.GroupIdAdapter;
import edu.nyu.grouper.xmpp.exceptions.GroupModificationException;

/**
 * Responds to Grouper changelog events by HTTP POSTing to the nakamura group servlets.
 */
public class HttpNakamuraGroupAdapter implements NakamuraGroupAdapter {
	
	private Log log = GrouperClientUtils.retrieveLog(HttpNakamuraGroupAdapter.class);
	
	private static String PROP_KEY_NAKAMURA_URL = "grouperClient.xmpp.nakamura.url";
	private static String PROP_KEY_NAKAMURA_USERNAME = "grouperClient.xmpp.nakamura.username";
	private static String PROP_KEY_NAKAMURA_PASSWORD = "grouperClient.xmpp.nakamura.password";
	
	private static String GROUP_CREATE_PATH = "/system/userManager/group.create.html";
	private static String GROUP_UPDATE_PATH_PREFIX = "/system/userManager/group/";

	private URL url;
	private String username;
	private String password;
	
	private InitialGroupPropertiesProvider initialPropertiesProvider;
	private GroupIdAdapter groupNameAdapter;

	public HttpNakamuraGroupAdapter() {
		initialPropertiesProvider = new StaticInitialGroupPropertiesProvider();
		groupNameAdapter = new BaseNakamuraGroupIdAdapter();
	}
	
	/**
	 * Read in config values from $GROUPER_CLIENT_HOME/conf/grouper.client.properties
	 * @throws MalformedURLException 
	 */
	public void configure() {
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("url", GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_URL, true));
		properties.put("username", GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_USERNAME, true));
		properties.put("password", GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_PASSWORD, true));
		configure(properties);
	}

	/**
	 * Configure using a HashMap
	 * @param properties
	 */
	public void configure(HashMap<String, String> properties){
		try {
			setUrl(new URL(GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_URL, true)));
			setUsername(GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_USERNAME, true));
			setPassword(GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_PASSWORD, true));
		}
		catch (MalformedURLException mfe){
			log.error("Could not parse the value of " + PROP_KEY_NAKAMURA_URL + " : " 
					+ GrouperClientUtils.propertiesValue(PROP_KEY_NAKAMURA_URL, false) );
			throw new RuntimeException(mfe.toString());
		}
		
	}

	/**
	 * POST to http://localhost:8080/system/userManager/group.create.html
	 * @see org.sakaiproject.nakamura.user.servlet.CreateSakaiGroupServlet
	 */
	public void createGroup(String groupId, String groupExtension) throws GroupModificationException {
		
		String nakamuraGroupName = groupNameAdapter.getNakamuraName(groupId);

		HttpClient client = getHttpClient();
		PostMethod method = new PostMethod(url.toString() + GROUP_CREATE_PATH);
	    method.addParameter(":name", nakamuraGroupName);
	    initialPropertiesProvider.addProperties(groupId, nakamuraGroupName, method);

	    try{
	    	int returnCode = client.executeMethod(method);
	    	method.getResponseBodyAsStream();

	    	switch (returnCode){
			case HttpStatus.SC_OK:
				log.debug("SUCCESS: created a group for " + nakamuraGroupName);
				break;
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				log.debug("FAILURE: Unable to create a group for " + nakamuraGroupName + ". Received an HTTP 500 response.");
				break;
			case HttpStatus.SC_FORBIDDEN:
				log.debug("FAILURE: Unable to create a group for " + nakamuraGroupName
						+ ". Received an HTTP 403 Forbidden. Check the username and password.");
				break;
			default:
				log.error("FAILURE: Unable to create a group for " + nakamuraGroupName
						+ "Unhandled reponse code : " + returnCode);
				break;
	    	}
	    } catch (Exception e) {
	      log.error("An exception occurred while creating the group. " + e.toString());
	    } finally {
	      method.releaseConnection();
	    }
	}

	/**
	 * Delete a group from sakai3
	 * curl -Fgo=1 http://localhost:8080/system/userManager/group/groupId.delete.html
	 */
	public void deleteGroup(String groupId, String groupExtension) throws GroupModificationException {
		
		String nakamuraGroupName = groupNameAdapter.getNakamuraName(groupId);

		HttpClient client = getHttpClient();
	    PostMethod method = new PostMethod(url.toString() + getDeletePath(nakamuraGroupName));
	    method.addParameter("go", "1");

	    try{
	    	int returnCode = client.executeMethod(method);
	    	method.getResponseBodyAsStream();

	    	switch (returnCode){
			case HttpStatus.SC_OK:
	    			log.debug("SUCCESS: deleted group " + nakamuraGroupName);
	    			break;	
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				log.debug("FAILURE: Unable to delete group " + nakamuraGroupName + ". Received an HTTP 500 response.");
				break;
			case HttpStatus.SC_FORBIDDEN:
				log.debug("FAILURE: Unable to create a group for " + nakamuraGroupName
						+ ". Received an HTTP 403 Forbidden. Check the username and password.");
				break;
			default:
				log.error("FAILURE: Unable to delete group " + nakamuraGroupName
		    				+ "Unhandled reponse code : " + returnCode);
				break;
	    	}
	    } catch (Exception e) {
	      log.error("An exception occurred while deleting the group. " + groupId 
	    		  + " Error: " + e.toString());
	    } finally {
	      method.releaseConnection();
	    }
	}

	/**
	 * Add a subjectId to a group by POSTing to:
	 * http://localhost:8080/system/userManager/group/groupId.update.html :member=subjectId
	 */
	public void addMembership(String groupId, String groupExtension, GrouperClientXmppSubject subject)
			throws GroupModificationException {
		PostMethod method = new PostMethod(url.toString() + getUpdatePath(groupId));
	    method.addParameter(":member", subject.getSourceId());
	    if (updateGroupMembership(groupId, subject.getSubjectId(), method)){
	    	log.info("SUCCESS: add subjectId=" + subject.getSourceId() + " to group=" + groupId );
	    }
	}

	/**
	 * Delete a subjectId from a group by POSTing to:
	 * http://localhost:8080/system/userManager/group/groupId.update.html :member=subjectId
	 */
	public void deleteMembership(String groupId, String groupExtension, GrouperClientXmppSubject subject)
			throws GroupModificationException {
		String nakamuraGroupName = groupNameAdapter.getNakamuraName(groupExtension);
		PostMethod method = new PostMethod(url.toString() + getUpdatePath(nakamuraGroupName));
	    method.addParameter(":member@Delete", subject.getSourceId());
	    if (updateGroupMembership(nakamuraGroupName, subject.getSubjectId(), method)){
	    	log.info("SUCCESS: deleted subjectId=" + subject.getSourceId() + " from group=" + nakamuraGroupName );
	    }
	}

	/**
	 * Add or delete a subject group membership.
	 * @param groupId the id of the group being modified.
	 * @param subjectId the id of the subject being added or remove.
	 * @param method the POST method to send to nakamura
	 * @return
	 */
	private boolean updateGroupMembership(String groupId, String subjectId, PostMethod method){
		HttpClient client = getHttpClient();
	    boolean success = false;
	    try{
	    	int returnCode = client.executeMethod(method);
	    	method.getResponseBodyAsStream();

	    	switch (returnCode){
			case HttpStatus.SC_OK:
				success = true;
				break;
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				log.error("FAILURE: Encountered a 500 error while modifing group="
						+ groupId);
				break;
			case HttpStatus.SC_NOT_FOUND:
				log.error("FAILURE: Nakamura reported that the group "
						+ groupId + " does not exist");
				break;
			case HttpStatus.SC_FORBIDDEN:
				log.debug("FAILURE: Unable to modify group  " + groupId
						+ ". Received an HTTP 403 Forbidden. Check the username and password.");
				break;
			default:
				log.error("FAILURE: Unable to modify subject membership: subject=" + subjectId 
						+ " group=" + groupId + ". Unhandled reponse code : " + returnCode);
				break;
	    	}
	    } catch (Exception e) {
	    	log.error("An exception occurred while modifying group membership. subjectId=" + subjectId + 
	    		  	" group=" + groupId + " Error: " + e.toString());
	    } finally {
	      method.releaseConnection();
	    }
	    return success;
	}

	private String getUpdatePath(String groupId){
		return GROUP_UPDATE_PATH_PREFIX + groupId + ".update.html";
	}

	private String getDeletePath(String groupId){
		return GROUP_UPDATE_PATH_PREFIX + groupId + ".delete.html";
	}

	private HttpClient getHttpClient(){
		HttpClient client = new HttpClient();
		HttpState state = client.getState();
		state.setCredentials(
			new AuthScope(getUrl().getHost(), getPort(getUrl())),
			new UsernamePasswordCredentials(getUsername(), getPassword()));
		client.getParams().setAuthenticationPreemptive(true);
		client.getParams().setParameter("http.useragent", this.getClass().getName());
		return client;
	}

	/**
	 * If you don't specify a port when creating a {@link URL} {@link URL#getPort()} will return -1.
	 * This function uses the default HTTP/s ports  
	 * @return the port for this.url. 80 or 433 if not specified.
	 */
	private int getPort(URL url){
		int port = url.getPort();
		if (port == -1){
			if (url.getProtocol().equals("http")){
				port = 80;
			}
			else if(url.getProtocol().equals("https")){
				port = 443;
			}
		}
		log.debug("port = " + port);
		return port;
	}
	
	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}