package edu.nyu.grouper;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.event.Event;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAddMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupDeleteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.nyu.grouper.api.GrouperIdHelper;
import edu.nyu.grouper.api.GrouperManager;
import edu.nyu.grouper.api.GrouperConfiguration;
import edu.nyu.grouper.util.GrouperHttpUtil;
import edu.nyu.grouper.util.GrouperJsonUtil;

@Service
@Component
public class GrouperManagerImpl implements GrouperManager {
	
	private static final Logger log = LoggerFactory.getLogger(GrouperManager.class);
	
	private static String GROUPER_NAME_PROP = "grouper:name";
	
	@Reference
	protected GrouperConfiguration grouperConfiguration;
	
	@Reference
	protected GrouperIdHelper groupIdHelper;
	
	@Reference
	protected Repository repository;

	private Session session;
	// Fetched via the Repository Session.
	private AuthorizableManager authorizableManager;
	
	@Activate
	public void activate(Map<?, ?> props) 
		throws ConfigurationException, ClientPoolException, StorageClientException, AccessDeniedException{

		session = repository.loginAdministrative();
		authorizableManager = session.getAuthorizableManager();
		log.debug("Activated!");
	}
	
	@Deactivate
	public void deactivate(){
		if (session != null) {
	        try {
	        	session.logout();
	        } catch (ClientPoolException e) {
	        	log.error(e.getLocalizedMessage(), e);
	        	throw new IllegalStateException(e);
	        }
		}
	}

	public void createGroup(String groupId) {
		try {
			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);

			if (!authorizable.isGroup()){
				log.error("{} is not a group", authorizable.getId());
				return;
			}
			
			String grouperNameProperty = (String)authorizable.getProperty("grouper:name");
			if (grouperNameProperty != null){
				if (log.isDebugEnabled()){
					log.debug("{}, already has grouper group: {}.", groupId, grouperNameProperty);
				}
				return;
			}

			String grouperName = groupIdHelper.getGrouperName(groupId);
			String grouperExtension = groupIdHelper.getGrouperExtension(groupId);

			log.debug("Creating a new Grouper Group = {} for sakai authorizableId = {}", 
					grouperName, groupId);

			// Fill out the group save request beans
			WsRestGroupSaveRequest groupSave = new WsRestGroupSaveRequest();
			WsGroupToSave wsGroupToSave = new WsGroupToSave();
			wsGroupToSave.setWsGroupLookup(new WsGroupLookup(grouperName, null));
			WsGroup wsGroup = new WsGroup();
			wsGroup.setDescription((String)authorizable.getProperty("sakai:group-description"));
			wsGroup.setDisplayExtension(grouperExtension);
			wsGroup.setExtension(grouperExtension);
			wsGroup.setName(grouperName);
			wsGroupToSave.setWsGroup(wsGroup);
			wsGroupToSave.setCreateParentStemsIfNotExist("T");
			groupSave.setWsGroupToSaves(new WsGroupToSave[]{ wsGroupToSave });

			JSONObject response = post("/groups", groupSave);

			authorizable.setProperty("grouper:name", grouperName);
			authorizableManager.updateAuthorizable(authorizable);

			log.debug("Success! Created a new Grouper Group = {} for sakai authorizableId = {}", 
					grouperName, groupId);
		}
		catch (StorageClientException sce) {
			log.error("Unable to fetch authorizable for " + groupId, sce);
		} 
		catch (AccessDeniedException ade) {
			log.error("Unable to fetch authorizable for " + groupId + ". Access Denied.", ade);
		}
		catch (GrouperException ge) {
			log.error("An error occurred while communicating with the grouper web services.", ge);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	/**
	 * @{inheritDoc}
	 */
	public void deleteGroup(String groupId) {

		try {
			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);

			if (!authorizable.isGroup()){
				log.error("{} is not a group", authorizable.getId());
				return;
			}
			Group group = (Group) authorizable;
			String grouperName = (String)group.getProperty(GROUPER_NAME_PROP);
			if (grouperName == null){
				grouperName = groupIdHelper.getGrouperName(group.getId());
			}
			
			log.debug("Deleting Grouper Group = {} for sakai authorizableId = {}",
					grouperName, group.getId());

			// Fill out the group delete request beans
			WsRestGroupDeleteRequest groupDelete = new WsRestGroupDeleteRequest();
			groupDelete.setWsGroupLookups(new WsGroupLookup[]{ new WsGroupLookup(grouperName, null) });

			JSONObject response = post("/groups", groupDelete);
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
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	/**
	 * @{inheritDoc}
	 */
	public void addMemberships(String groupId, Collection<String> membersToAdd){
		try {
			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);

			if (!authorizable.isGroup()){
				log.error("{} is not a group", authorizable.getId());
				return;
			}

			String grouperName = groupIdHelper.getGrouperName(groupId);
			String membersString = StringUtils.join(membersToAdd, ',');
			log.debug("Adding members: Group = {} members = {}", 
						grouperName, membersString);

			WsRestAddMemberRequest addMembers = new WsRestAddMemberRequest();
			// Don't overwrite the entire group membership. just add to it.
			addMembers.setReplaceAllExisting("F");

			// Each subjectId must have a lookup 
			WsSubjectLookup[] subjectLookups = new WsSubjectLookup[membersToAdd.size()];
			int  i = 0;
			for (String subjectId: membersToAdd){
				subjectLookups[i] = new WsSubjectLookup(subjectId, null, null);
				i++;
			}
			addMembers.setSubjectLookups(subjectLookups);

			String urlPath = "/groups/" + grouperName + "/members";
			urlPath = urlPath.replace(":", "%3A");
			JSONObject response = post(urlPath, addMembers);

			log.debug("Success! Added members: Group = {} members = {}", 
					grouperName, membersString);
		}
		catch (StorageClientException sce) {
			log.error("Unable to fetch authorizable for " + groupId, sce);
		} 
		catch (AccessDeniedException ade) {
			log.error("Unable to fetch authorizable for " + groupId + ". Access Denied.", ade);
		}
		catch (GrouperException ge) {
			log.error("An error occurred while communicating with the grouper web services.", ge);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @{inheritDoc}
	 */
	public void removeMemberships(String groupId, Collection<String> membersToRemove){
		try {
			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);

			if (!authorizable.isGroup()){
				log.error("{} is not a group", authorizable.getId());
				return;
			}

			String grouperName = groupIdHelper.getGrouperName(groupId);
			String membersString = StringUtils.join(membersToRemove, ',');
			log.debug("Removing members: Group = {} members = {}", 
						grouperName, membersString);

			WsRestDeleteMemberRequest deleteMembers = new WsRestDeleteMemberRequest();
			// Each subjectId must have a lookup 
			WsSubjectLookup[] subjectLookups = new WsSubjectLookup[membersToRemove.size()];
			int  i = 0;
			for (String subjectId: membersToRemove){
				subjectLookups[i] = new WsSubjectLookup(subjectId, null, null);
				i++;
			}
			deleteMembers.setSubjectLookups(subjectLookups);
			String urlPath = "/groups/" + grouperName + "/members";
			urlPath = urlPath.replace(":", "%3A");
			JSONObject response = post(urlPath, deleteMembers);

			log.debug("Success! Added members: Group = {} members = {}", 
					grouperName, membersString);
		}
		catch (StorageClientException sce) {
			log.error("Unable to fetch authorizable for " + groupId, sce);
		} 
		catch (AccessDeniedException ade) {
			log.error("Unable to fetch authorizable for " + groupId + ". Access Denied.", ade);
		}
		catch (GrouperException ge) {
			log.error("An error occurred while communicating with the grouper web services.", ge);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * @{inheritDoc}
	 */
	public void updateGroup(String groupId, Event event) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Issue an HTTP POST to Grouper Web Services
	 * @param grouperRequestBean a Grouper WS bean representing a grouper action
	 * @return the parsed JSON response
	 * @throws HttpException
	 * @throws IOException
	 * @throws GrouperException
	 */
	private JSONObject post(String urlPath, Object grouperRequestBean) throws HttpException, IOException, GrouperException  {
		
		// URL e.g. http://localhost:9090/grouper-ws/servicesRest/v1_6_003/...
		HttpClient client = GrouperHttpUtil.getHttpClient(grouperConfiguration);		            
		String grouperWsRestUrl = grouperConfiguration.getRestWsUrlString() + urlPath;
        PostMethod method = new PostMethod(grouperWsRestUrl);
        method.setRequestHeader("Connection", "close");

	    // Encode the request and send it off
	    String requestDocument = GrouperJsonUtil.toJSONString(grouperRequestBean);
	    method.setRequestEntity(new StringRequestEntity(requestDocument, "text/x-json", "UTF-8"));

	    int responseCode = client.executeMethod(method);
	    log.debug("POST to {} . response code {}", grouperWsRestUrl, responseCode);

	    // Check the response
	    Header successHeader = method.getResponseHeader("X-Grouper-success");
	    String successString = successHeader == null ? null : successHeader.getValue();
	    if (successString == null || successString.equals("")) {
	    	throw new GrouperException("The Grouper WS did not respond.");
	    }
	    
	    String resultCode = method.getResponseHeader("X-Grouper-resultCode").getValue();
	    String responseString = IOUtils.toString(method.getResponseBodyAsStream());
	    
	    if (!"T".equals(successString)) {
	    	throw new GrouperException("Bad response from web service: successString: " + successString 
	    			+ ", resultCode: " + resultCode + ", " + responseString);
	    }

	    return JSONObject.fromObject(responseString);
	}
}