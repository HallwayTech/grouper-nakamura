package edu.nyu.grouper.osgi;

import java.io.IOException;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
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

import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupSaveRequest;
import edu.nyu.grouper.BaseGrouperIdHelper;
import edu.nyu.grouper.api.GrouperIdHelper;
import edu.nyu.grouper.osgi.api.GrouperConfiguration;
import edu.nyu.grouper.util.GrouperHttpUtil;
import edu.nyu.grouper.util.GrouperJsonUtil;

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

	@Reference
	protected GrouperConfiguration grouperConfiguration;
	
	@Reference
	protected Repository repository;

	// Fetched via the repository session.
	private AuthorizableManager authorizableManager;
	
	private GrouperIdHelper groupIdAdapter;

	@Activate
	public void activate(Map<?, ?> props) 
		throws ConfigurationException, ClientPoolException, StorageClientException, AccessDeniedException{
		
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
					
					if (authorizable.getProperty("grouper:uuid") != null){
						if (log.isDebugEnabled()){
							log.debug("This group was created by grouper. No need to send it back.");
						}
						return;
					}

					String fullGrouperName = groupIdAdapter.getFullGrouperName(
							grouperConfiguration.getBaseStem(), groupId);
					String grouperName = groupIdAdapter.getName(groupId);
					
					log.debug("Creating a new Grouper Group = {} for sakai authorizableId = {}", 
								fullGrouperName, groupId);

					// URL e.g. http://localhost:9090/grouper-ws/servicesRest/v1_6_003/...
					HttpClient client = GrouperHttpUtil.getHttpClient(grouperConfiguration.getUrl(), 
							grouperConfiguration.getUsername(),
							grouperConfiguration.getPassword());		            
					String grouperWsRestUrl = grouperConfiguration.getUrl() + "/" + grouperConfiguration.getWsVersion() + "/groups";
		            PostMethod method = new PostMethod(grouperWsRestUrl);
		            method.setRequestHeader("Connection", "close");

		            // Fill out the group save request beans
					WsRestGroupSaveRequest groupSave = new WsRestGroupSaveRequest();
				    WsGroupToSave wsGroupToSave = new WsGroupToSave();
				    wsGroupToSave.setWsGroupLookup(new WsGroupLookup(fullGrouperName, null));
				    WsGroup wsGroup = new WsGroup();
				    wsGroup.setDescription((String)authorizable.getProperty("sakai:group-description"));
				    wsGroup.setDisplayExtension(grouperName);
				    wsGroup.setExtension(grouperName);
				    wsGroup.setName(fullGrouperName);
				    wsGroupToSave.setWsGroup(wsGroup);
				    groupSave.setWsGroupToSaves(new WsGroupToSave[]{ wsGroupToSave });
				    
				    log.debug("Group beans created.");

				    // Encode the request and send it off
				    String requestDocument = GrouperJsonUtil.toJSONString(groupSave);
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
				    String responseString = IOUtils.toString(method.getResponseBodyAsStream());
				    // JSONObject responseJSON = JSONObject.fromObject(responseString);

				    if (!success) {
				    	throw new Exception("Bad response from web service: successString: " + successString 
				    			+ ", resultCode: " + resultCode + ", " + responseString);
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
			} 
			catch (RuntimeException e) {
				log.error(e.getMessage(), e);
			}
			catch (Exception e) {
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
}