package edu.nyu.grouper.esb;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONStringer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.nyu.grouper.api.NakamuraGroupAdapter;
import edu.nyu.grouper.util.api.GroupIdAdapter;
import edu.nyu.grouper.util.api.InitialGroupPropertiesProvider;
import edu.nyu.grouper.exceptions.GroupModificationException;

/**
 * Responds to Grouper changelog events by HTTP POSTing to the nakamura group servlets.
 */
public class HttpNakamuraGroupAdapter implements NakamuraGroupAdapter {
	
	private Log log = LogFactory.getLog(HttpNakamuraGroupAdapter.class);
	
	private static String GROUP_CREATE_PATH = "/system/userManager/group.create.json";
	private static String GROUP_UPDATE_PATH_PREFIX = "/system/userManager/group/";

	private URL url;
	private String username;
	private String password;
	
	// Sets HTTP POST params that are stored in nakamura as properties on the group. 
	private InitialGroupPropertiesProvider initialPropertiesProvider;
	
	// Maps group names from grouper <-> nakamura
	private GroupIdAdapter groupIdAdapter;

	/**
	 * POST to http://localhost:8080/system/userManager/group.create.json
	 * @see org.sakaiproject.nakamura.user.servlet.CreateSakaiGroupServlet
	 */
	public void createGroup(Group group) throws GroupModificationException {
		
		String nakamuraGroupName = groupIdAdapter.getNakamuraName(group.getExtension());

		HttpClient client = getHttpClient();
		PostMethod method = new PostMethod(url.toString() + GROUP_CREATE_PATH);
	    method.addParameter(":name", nakamuraGroupName);
	    initialPropertiesProvider.addProperties(group, nakamuraGroupName,  method);
	    String errorMessage = null;

	    try{
	    	int returnCode = client.executeMethod(method);
	    	InputStream response = method.getResponseBodyAsStream();

	    	switch (returnCode){
	    	// 200
			case HttpStatus.SC_OK:
				if (log.isInfoEnabled()){
					log.info("SUCCESS: created a group for " + group.getName());
				}
				break;
			// 400
			case HttpStatus.SC_BAD_REQUEST:
				// Parse the response and check the status.message
				JSONObject jsonObject = JSONObject.fromObject(IOUtils.toString(response));
				String statusMessage = jsonObject.getString("status.message"); 

				if (statusMessage.startsWith("A principal already exists")){
					if (log.isInfoEnabled()){
						log.info("Create event for a group that already exists: " + group.getName());
					}
				}
				else {
					errorMessage = "FAILURE: 400 : Unable to create a group for " + group.getName() + ". " + statusMessage;
				}
				break;
			// 403
			case HttpStatus.SC_FORBIDDEN: 
				errorMessage = "FAILURE: 403: Unable to create a group for " + group.getName()
						+ ". Check the username and password.";
				break;
			// 500
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				errorMessage = "FAILURE: 500: Unable to create a group for " + group.getName();
				break;
			// ?
			default:
				errorMessage = "FAILURE: " + returnCode + ": Unable to create a group for " + group.getName();
				logUnhandledResponse(returnCode, response);
				break;
	    	}
	    }
	    catch (Exception e) {
	    	errorMessage = "An exception occurred while creating the group. " + e.toString();
	    } 
	    finally {
	    	method.releaseConnection();
	    }

	    if (errorMessage != null){
	    	if (log.isErrorEnabled()){ 
	    		log.error(errorMessage);
	    	}
	    	throw new GroupModificationException(errorMessage);
	    }
	}

	/**
	 * Delete a group from sakai3
	 * curl -Fgo=1 http://localhost:8080/system/userManager/group/groupId.delete.html
	 */
	public void deleteGroup(String groupId, String groupName) throws GroupModificationException {

		String nakamuraGroupName = groupIdAdapter.getNakamuraName(groupName);

		HttpClient client = getHttpClient();
	    PostMethod method = new PostMethod(url.toString() + getDeletePath(nakamuraGroupName));
	    method.addParameter("go", "1");
	    String errorMessage = null;

	    try{
	    	int returnCode = client.executeMethod(method);
	    	InputStream response = method.getResponseBodyAsStream();

	    	switch (returnCode){
			case HttpStatus.SC_OK:
			case HttpStatus.SC_CREATED:
				if (log.isInfoEnabled()){
	    			log.info("SUCCESS: deleted group " + nakamuraGroupName);
				}
	    		break;	
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				errorMessage = "FAILURE: Unable to delete group " + nakamuraGroupName + 
						". Received an HTTP 500 response.";
				break;
			case HttpStatus.SC_FORBIDDEN:
				errorMessage = "FAILURE: Unable to create a group for " + nakamuraGroupName
						+ ". Received an HTTP 403 Forbidden. Check the username and password.";
				break;
			default:
				errorMessage = "FAILURE: Unable to delete group " + nakamuraGroupName;
				logUnhandledResponse(returnCode, response);
				break;
	    	}
	    } catch (Exception e) {
	    	errorMessage = "An exception occurred while deleting the group. " + groupId
	    		  			+ " Error: " + e.toString();
	    } finally {
	      method.releaseConnection();
	    }
	    
	    if (errorMessage != null){
	    	if (log.isErrorEnabled()){ 
	    		log.error(errorMessage);
	    	}
	    	throw new GroupModificationException(errorMessage);
	    }
	}

	/**
	 * Add a subjectId to a group by POSTing to:
	 * http://localhost:8080/system/userManager/group/groupId.update.html :member=subjectId
	 */
	public void addMembership(String groupId, String groupName, String subjectId)
			throws GroupModificationException {
		PostMethod method = new PostMethod(url.toString() + getUpdatePath(groupId));
	    method.addParameter(":member", subjectId);
	    updateGroupMembership(groupId, subjectId, method);
	    if (log.isInfoEnabled()){
	    	log.info("SUCCESS: add subjectId=" + subjectId + " to group=" + groupId );
	    }
	}

	/**
	 * Delete a subjectId from a group by POSTing to:
	 * http://localhost:8080/system/userManager/group/groupId.update.html :member=subjectId
	 */
	public void deleteMembership(String groupId, String groupName, String subjectId)
			throws GroupModificationException {
		String nakamuraGroupName = groupIdAdapter.getNakamuraName(groupName);
		PostMethod method = new PostMethod(url.toString() + getUpdatePath(nakamuraGroupName));
	    method.addParameter(":member@Delete", subjectId);
	    updateGroupMembership(nakamuraGroupName, subjectId, method);
	    if (log.isInfoEnabled()){
	    	log.info("SUCCESS: deleted subjectId=" + subjectId + " from group=" + nakamuraGroupName );
	    }
	}

	/**
	 * Add or delete a subject group membership.
	 * @param groupName the id of the group being modified.
	 * @param subjectId the id of the subject being added or remove.
	 * @param method the POST method to send to nakamura
	 * @return
	 */
	private void updateGroupMembership(String groupName, String subjectId, PostMethod method) throws GroupModificationException {
		HttpClient client = getHttpClient();
	    String errorMessage = null;
	    try{
	    	int returnCode = client.executeMethod(method);
	    	InputStream reponse = method.getResponseBodyAsStream();

	    	switch (returnCode){
			case HttpStatus.SC_OK:
				break;
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				errorMessage = "FAILURE: Encountered a 500 error while modifing group=" + groupName;
				break;
			case HttpStatus.SC_NOT_FOUND:
				errorMessage = "FAILURE: Nakamura reported that the group " + groupName + " does not exist";
				break;
			case HttpStatus.SC_FORBIDDEN:
				errorMessage = "FAILURE: Unable to modify group  " + groupName
						+ ". Received an HTTP 403 Forbidden. Check the username and password.";
				break;
			default:
				errorMessage = "FAILURE: Unable to modify subject membership: subject=" + subjectId 
						+ " group=" + groupName;
				logUnhandledResponse(returnCode, reponse);
				break;
	    	}
	    } catch (Exception e) {
	    	errorMessage = "An exception occurred while modifying group membership. subjectId=" + subjectId + 
	    		  	" group=" + groupName + " Error: " + e.toString();
	    } finally {
	      method.releaseConnection();
	    }
	    
	    if (errorMessage != null){
	    	if (log.isErrorEnabled()){
	    		log.error(errorMessage);
	    	}
	    	throw new GroupModificationException(errorMessage);
	    }
	}

	private String getUpdatePath(String groupId){
		return GROUP_UPDATE_PATH_PREFIX + groupId + ".update.html";
	}

	private String getDeletePath(String groupId){
		return GROUP_UPDATE_PATH_PREFIX + groupId + ".delete.html";
	}

	/**
	 * Construct an {@link HttpClient} which is configured to authenticate to Nakamura.
	 * @return the configured client.
	 */
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
		return port;
	}
	
	public void logUnhandledResponse(int responseCode, InputStream response){
		if (log.isErrorEnabled()){
			try {
				log.error("Unhandled response. code=" + responseCode + "\nResponse: " + IOUtils.toString(response));
			} catch (IOException e) {
				log.error("Error reading the response", e);
			}
		}
	}
	
	public void setInitialPropertiesProvider(
			InitialGroupPropertiesProvider initialPropertiesProvider) {
		this.initialPropertiesProvider = initialPropertiesProvider;
	}

	public void setGroupIdAdapter(GroupIdAdapter groupIdAdapter) {
		this.groupIdAdapter = groupIdAdapter;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(String urlString){
		try {
			setUrl(new URL(urlString));
		}
		catch (MalformedURLException mfe){
			log.error("Could not parse " + urlString + "into a String");
			throw new RuntimeException(mfe.toString());
		}
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