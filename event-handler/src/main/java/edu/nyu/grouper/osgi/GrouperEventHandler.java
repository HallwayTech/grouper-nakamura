package edu.nyu.grouper.osgi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import net.sf.json.JSONSerializer;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.util.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.ws.rest.WsRestResultProblem;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestRequestContentType;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestResponseContentType;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupDeleteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupSaveRequest;

import edu.nyu.grouper.BaseGrouperIdHelper;
import edu.nyu.grouper.api.GrouperIdHelper;

@Service(value = EventHandler.class)
@Component(immediate = true, metatype=true)
@Properties(value = { 
		@Property(name = EventConstants.EVENT_TOPIC, 
				value = {
				"org/sakaiproject/nakamura/lite/authorizables/ADDED",
				"org/sakaiproject/nakamura/lite/authorizables/DELETE"
		})
})
public class GrouperEventHandler implements EventHandler {

	private static final Logger log = LoggerFactory.getLogger(GrouperEventHandler.class);

	// Configurable via the ConfigAdmin services.
	private static final String DEFAULT_URL = "http://localhost:8080/grouper-ws/servicesRest";
	@Property(value=DEFAULT_URL)
	private static final String PROP_URL = "sakai.grouper.url";
	
	private static final String DEFAULT_WS_VERSION = "1_6_000";
	@Property(value=DEFAULT_WS_VERSION)
	private static final String PROP_WS_VERSION= "sakai.grouper.ws_version";

	private static final String DEFAULT_USERNAME = "GrouperSystem";
	@Property(value=DEFAULT_USERNAME)
	private static final String PROP_USERNAME = "sakai.grouper.username";

	private static final String DEFAULT_PASSWORD = "abc123";
	@Property(value=DEFAULT_PASSWORD)
	private static final String PROP_PASSWORD = "sakai.grouper.password";

	private static final String DEFAULT_BASESTEM = "edu:apps:sakai3";
	@Property(value=DEFAULT_BASESTEM)
	private static final String PROP_BASESTEM = "sakai.grouper.basestem";
	
	// Grouper configuration.
	private URL url;
	private String wsVersion;
	private String username;
	private String password;
	private String baseStem;

	@Reference
	protected Repository repository;

	// Fetched via the repository session.
	private AuthorizableManager authorizableManager;
	
	private GrouperIdHelper groupIdAdapter;
	
	@SuppressWarnings("rawtypes")
	@Activate
	public void activate(Map props) 
		throws ConfigurationException, ClientPoolException, StorageClientException, AccessDeniedException{

		// Initial configuration
		updated(props);
		
		try {
			// Intialize the Nakamura Session
			authorizableManager = repository.loginAdministrative().getAuthorizableManager();
		
			// TODO: Make this a service reference
			groupIdAdapter = new BaseGrouperIdHelper();
					}
		catch (Exception e) {
			log.debug(e.getMessage());
		}
		
		log.debug("Activated!");
	}

	// -------------------------- Configuration Admin --------------------------
	/**
	 * Called by the Configuration Admin service when a new configuration is detected.
	 * @see org.osgi.service.cm.ManagedService#updated
	 */
	@SuppressWarnings("rawtypes")
	@Modified
	public void updated(Map props) throws ConfigurationException {
		try {
			url = new URL(OsgiUtil.toString(props.get(PROP_URL), DEFAULT_URL));
			wsVersion = OsgiUtil.toString(props.get(PROP_WS_VERSION), DEFAULT_WS_VERSION);
			username = OsgiUtil.toString(props.get(PROP_USERNAME), DEFAULT_USERNAME);
			password = OsgiUtil.toString(props.get(PROP_PASSWORD), DEFAULT_PASSWORD);
			baseStem = OsgiUtil.toString(props.get(PROP_BASESTEM), DEFAULT_BASESTEM);
		}
		catch (MalformedURLException mfe) {
			throw new ConfigurationException(PROP_URL, mfe.getMessage(), mfe);
		}
	}
	
	/**
	 * Respond to Group events in Nakamura by issuing WS calls to Grouper. 
	 */
	public void handleEvent(Event event) {

		logEvent(event);

		if ("org/sakaiproject/nakamura/lite/authorizables/ADDED".equals(event.getTopic())){
			String groupId = (String) event.getProperty("path");

			try {
				Authorizable authorizable = authorizableManager.findAuthorizable(groupId);
				if (authorizable.isGroup()){
					String fullGrouperName = groupIdAdapter.getFullGrouperName(baseStem, groupId);
					String grouperName = groupIdAdapter.getName(groupId);
					
					log.debug("Creating a new Grouper Group = {} for sakai authorizableId = {}", 
								fullGrouperName, groupId);

					HttpClient client = getHttpClient();
		            // URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_6_000/...
					String grouperWsRestUrl = url + "/" + wsVersion + "/groups";
		            PostMethod method = new PostMethod(grouperWsRestUrl);
		            method.setRequestHeader("Connection", "close");

		            // Fill out the group save request beans
					WsRestGroupSaveRequest groupSave = new WsRestGroupSaveRequest();
				    WsGroupToSave wsGroupToSave = new WsGroupToSave();
				    wsGroupToSave.setWsGroupLookup(new WsGroupLookup(fullGrouperName, null));
				    WsGroup wsGroup = new WsGroup();
				    wsGroup.setDescription("Created by Sakai3");
				    wsGroup.setDisplayExtension(grouperName);
				    wsGroup.setExtension(grouperName);
				    wsGroup.setName(fullGrouperName);
				    wsGroupToSave.setWsGroup(wsGroup);
				    groupSave.setWsGroupToSaves(new WsGroupToSave[]{ wsGroupToSave });
				    
				    log.debug("Group beans created.");

				    // Encode the request and send it off
				    String requestDocument = JSONSerializer.toJSON(groupSave).toString();
				    method.setRequestEntity(new StringRequestEntity(requestDocument, "text/x-json", "UTF-8"));
				    
				    log.debug("POST Method prepared for {} \n{}.", grouperWsRestUrl, requestDocument);
				    
				    client.executeMethod(method);
				    
				    log.debug("POST Method executed to {}.", grouperWsRestUrl);

				    // Check the response
				    Header successHeader = method.getResponseHeader("X-Grouper-success");
				    String successString = successHeader == null ? null : successHeader.getValue();
				    if (successString == null || successString.equals("")) {
				    	throw new Exception("Web service did not even respond!");
				    }
				    boolean success = "T".equals(successString);
				    String resultCode = method.getResponseHeader("X-Grouper-resultCode").getValue();
				    Object result = WsRestResponseContentType.json.parseString(
				    		IOUtils.toString(method.getResponseBodyAsStream()));

				    //see if problem
				    if (result instanceof WsRestResultProblem) {
				    	throw new RuntimeException(((WsRestResultProblem)result).getResultMetadata().getResultMessage());
				    }

				    //convert to object (from xhtml, xml, json, etc)
				    WsGroupSaveResults wsGroupSaveResults = (WsGroupSaveResults)result;
				    String resultMessage = wsGroupSaveResults.getResultMetadata().getResultMessage();

				    // see if request worked or not
				    if (!success) {
				    	throw new Exception("Bad response from web service: successString: " + successString 
				    			+ ", resultCode: " + resultCode + ", " + resultMessage);
				    }
				    
				    log.debug("Success! Created a new Grouper Group = {} for sakai authorizableId = {}", 
							fullGrouperName, groupId);
				}
				else {
					log.error("Group event : " + event.getTopic() + " fired for non group :" + authorizable.getId());
				}
			}
			catch (StorageClientException sce) {
				log.error("Unable to fetch authorizable for " + groupId, sce);
			} 
			catch (AccessDeniedException ade) {
				log.error("Unable to fetch authorizable for " + groupId + ". Access Denied.", ade);
			}
			catch (IOException ioe){
				log.error("IOException while communicating with grouper web services.", ioe);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		if ("org/sakaiproject/nakamura/lite/authorizables/DELETE".equals(event.getTopic())){
			String groupId = (String) event.getProperty("path");

			try {
				String fullGrouperName = groupIdAdapter.getFullGrouperName(baseStem, groupId);

				log.debug("Deleting Grouper Group = {} for sakai authorizableId = {}", 
						fullGrouperName, groupId);

				HttpClient client = getHttpClient();
				String grouperWsRestUrl = url + "/" + wsVersion + "/groups";
				PostMethod method = new PostMethod(grouperWsRestUrl);
				method.setRequestHeader("Connection", "close");

				// Fill out the group delete request beans
				WsRestGroupDeleteRequest groupDelete = new WsRestGroupDeleteRequest();
				groupDelete.setWsGroupLookups(new WsGroupLookup[]{ new WsGroupLookup(fullGrouperName, null) });

				log.debug("Group beans created.");

				// Encode the request and send it off
				String requestDocument = JSONSerializer.toJSON(groupDelete).toString();
 				method.setRequestEntity(new StringRequestEntity(requestDocument, "text/x-json", "UTF-8"));

				log.debug("POST Method prepared for {} \n{}.", grouperWsRestUrl, requestDocument);

				client.executeMethod(method);

				log.debug("POST Method executed to {}.", grouperWsRestUrl);

				// Check the response
				Header successHeader = method.getResponseHeader("X-Grouper-success");
				String successString = successHeader == null ? null : successHeader.getValue();
				if (successString == null || successString.equals("")) {
					throw new Exception("Web service did not even respond!");
				}
				boolean success = "T".equals(successString);
				String resultCode = method.getResponseHeader("X-Grouper-resultCode").getValue();
				Object result = WsRestResponseContentType.json.parseString(
						IOUtils.toString(method.getResponseBodyAsStream()));

				//see if problem
				if (result instanceof WsRestResultProblem) {
					throw new RuntimeException(((WsRestResultProblem)result).getResultMetadata().getResultMessage());
				}

				//convert to object (from xhtml, xml, json, etc)
				WsGroupSaveResults wsGroupSaveResults = (WsGroupSaveResults)result;
				String resultMessage = wsGroupSaveResults.getResultMetadata().getResultMessage();

				// see if request worked or not
				if (!success) {
					throw new Exception("Bad response from web service: successString: " + successString 
							+ ", resultCode: " + resultCode + ", " + resultMessage);
				}

				log.debug("Success! Delete Grouper Group = {} for sakai authorizableId = {}", 
						fullGrouperName, groupId);
			}
			catch (StorageClientException sce) {
				log.error("Unable to fetch authorizable for " + groupId, sce);
			} 
			catch (AccessDeniedException ade) {
				log.error("Unable to fetch authorizable for " + groupId + ". Access Denied.", ade);
			}
			catch (IOException ioe){
				log.error("IOException while communicating with grouper web services.", ioe);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Print the event to the log.
	 * @param event
	 */
	private void logEvent(Event event) {
		if (log.isInfoEnabled()){
			StringBuffer buffer = new StringBuffer();
			for(String name: Iterables.of(event.getPropertyNames())){
				buffer.append("\n" + name + "=" + event.getProperty(name));
			}
			log.info("\ntopic : " + event.getTopic() + " properties: " + buffer.toString());
		}
	}

	/**
	 * Construct an {@link HttpClient} which is configured to authenticate to Nakamura.
	 * @return the configured client.
	 */
	private HttpClient getHttpClient(){
		HttpClient client = new HttpClient();

		DefaultHttpParams.getDefaultParams().setParameter(
                HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

		HttpState state = client.getState();
		state.setCredentials(
				new AuthScope(url.getHost(), getPort(url)),
				new UsernamePasswordCredentials(username, password));
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
}