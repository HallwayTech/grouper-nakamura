/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.grouper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
import org.sakaiproject.nakamura.grouper.api.GrouperManager;
import org.sakaiproject.nakamura.grouper.exception.GrouperException;
import org.sakaiproject.nakamura.grouper.exception.GrouperWSException;
import org.sakaiproject.nakamura.grouper.exception.InvalidGroupIdException;
import org.sakaiproject.nakamura.grouper.name.BaseGrouperNameProvider;
import org.sakaiproject.nakamura.grouper.name.ContactsGrouperNameProviderImpl;
import org.sakaiproject.nakamura.grouper.name.api.GrouperNameManager;
import org.sakaiproject.nakamura.grouper.util.GrouperHttpUtil;
import org.sakaiproject.nakamura.grouper.util.GrouperJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDetail;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAddMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupDeleteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

@Service
@Component
public class GrouperManagerImpl implements GrouperManager {

	private static final Logger log = LoggerFactory.getLogger(GrouperManagerImpl.class);

	/*
	 * This is a special kind of grouper group that maintains adhoc memberships
	 * along with a group that is considered the system of record.
	 *
	 * To represent group1 we have in grouper:
	 *
	 * 1.  group1 include
	 * 2.  group1 exclude
	 * 3.  group1 system of record
	 * 4.  group1 system of record union include
	 * 5.  group1, (4) - (2).  This is the final group.
	 */
	private static final String INCLUDE_EXCLUDE_GROUP_TYPE = "addIncludeExclude";
	private static final String INCLUDE_SUFFIX = "_includes";
	private static final String EXCLUDE_SUFFIX = "_excludes";

	private static final String SYSTEM_OF_RECORD_SUFFIX = "_systemOfRecord";

	@Reference
	protected GrouperConfiguration grouperConfiguration;

	@Reference
	protected GrouperNameManager grouperNameManager;

	@Reference
	protected Repository repository;

	/**
	 * @{inheritDoc}
	 */
	public void createGroup(String groupId, Set<String> groupTypes) throws GrouperException {
		try {
			// Check if the groupid corresponds to a group.
			Session session = repository.loginAdministrative(grouperConfiguration.getIgnoredUserId());
			AuthorizableManager authorizableManager = session.getAuthorizableManager();
			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);

			if (!authorizable.isGroup()){
				log.error("{} is not a group", authorizable.getId());
				return;
			}

			String grouperName = grouperNameManager.getGrouperName(groupId);
			String grouperExtension = BaseGrouperNameProvider.getGrouperExtension(groupId, grouperConfiguration);

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

			// More detailed group info
			if (groupTypes != null && groupTypes.size() > 0 ) {
				// TODO: handle multiple group types or make the arg a single
				String groupType = groupTypes.iterator().next();
				WsGroupDetail groupDetail = new WsGroupDetail();
				groupDetail.setTypeNames(new String[] { groupType });
				wsGroup.setDetail(groupDetail);
				if (groupType.equals(INCLUDE_EXCLUDE_GROUP_TYPE)){
					wsGroup.setName(grouperName + SYSTEM_OF_RECORD_SUFFIX);
					wsGroup.setDisplayExtension(grouperExtension + SYSTEM_OF_RECORD_SUFFIX);
					wsGroup.setExtension(grouperExtension + SYSTEM_OF_RECORD_SUFFIX);
				}
			}

			// Package up the request
			wsGroupToSave.setWsGroup(wsGroup);
			wsGroupToSave.setCreateParentStemsIfNotExist("T");
			groupSave.setWsGroupToSaves(new WsGroupToSave[]{ wsGroupToSave });

			// POST and parse the response
			JSONObject response = post("/groups", groupSave);
			WsGroupSaveResults results = (WsGroupSaveResults)JSONObject.toBean(
					response.getJSONObject("WsGroupSaveResults"), WsGroupSaveResults.class);

			// Error handling is a bit awkward. If the group already exists its not a problem
			if (!"T".equals(results.getResultMetadata().getSuccess())) {
				if (results.getResults()[0].getResultMetadata().getResultMessage().contains("already exists")){
					log.debug("Group already existed in grouper at {}", grouperName);
				}
				else {
					throw new GrouperWSException(results);
				}
			}

			authorizable.setProperty(GROUPER_NAME_PROP, grouperName);
			authorizableManager.updateAuthorizable(authorizable);
			session.logout();

			log.debug("Success: Created a new Grouper Group = {} for sakai authorizableId = {}",
					grouperName, groupId);
		}
		catch (StorageClientException sce) {
			throw new GrouperException("Unable to fetch authorizable for " + groupId, sce);
		}
		catch (AccessDeniedException ade) {
			throw new GrouperException("Unable to fetch authorizable for " + groupId + ". Access Denied.", ade);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * If the attributes contains a grouper name use it to delete the group in grouper.
	 * Else call delete(groupid) which will try to resolve the grouper name with the GrouperNameManager
	 *
	 */
	public void deleteGroup(String nakamuraGroupId, Map<String, Object> attributes) throws GrouperException{
		String grouperName = null;
		if (attributes != null ){
			grouperName = (String)attributes.get(GROUPER_NAME_PROP);
		}
		if (grouperName != null){
			deleteFromGrouper(grouperName);
		}
		else {
			deleteGroup(nakamuraGroupId);
		}
	}

	/**
	 * @{inheritDoc}
	 */
	public void deleteGroup(String nakamuraGroupId) throws GrouperException {
		String grouperName = grouperNameManager.getGrouperName(nakamuraGroupId);
		deleteFromGrouper(grouperName);
		log.debug("Deleted Grouper Group = {} for sakai authorizableId = {}", grouperName, nakamuraGroupId);
	}

	/**
	 *
	 * @param groupIdentifier either the grouper name or uuid
	 * @throws GrouperException
	 */
	private void deleteFromGrouper(String groupIdentifier) throws GrouperException{
		try {
			// Fill out the group delete request beans
			WsRestGroupDeleteRequest groupDelete = new WsRestGroupDeleteRequest();
			if (grouperConfiguration.getGroupTypes().contains(INCLUDE_EXCLUDE_GROUP_TYPE)){
				groupDelete.setWsGroupLookups(new WsGroupLookup[]{
						new WsGroupLookup(groupIdentifier, null),
						new WsGroupLookup(groupIdentifier + INCLUDE_SUFFIX, null),
						new WsGroupLookup(groupIdentifier + EXCLUDE_SUFFIX, null) });
			} else {
				groupDelete.setWsGroupLookups(new WsGroupLookup[]{ new WsGroupLookup(groupIdentifier, null)});
			}

			// Send the request and parse the result, throwing an exception on failure.
			JSONObject response = post("/groups", groupDelete);
			WsGroupDeleteResults results = (WsGroupDeleteResults)JSONObject.toBean(
					response.getJSONObject("WsGroupDeleteResults"), WsGroupDeleteResults.class);
			if (!"T".equals(results.getResultMetadata().getSuccess())) {
					throw new GrouperWSException(results);
			}
		}
		catch (Exception e) {
			throw new GrouperException(e.getMessage());
		}
	}

	/**
	 * @{inheritDoc}
	 */
	public void addMemberships(String groupId, Collection<String> membersToAdd) throws GrouperException{
		checkGroupId(groupId);

		// Resolve the grouper name
		String grouperName = grouperNameManager.getGrouperName(groupId);
		String membersString = StringUtils.join(membersToAdd, ',');
		log.debug("Adding members: Group = {} members = {}", grouperName, membersString);

		if ( groupId.startsWith(ContactsGrouperNameProviderImpl.CONTACTS_GROUPID_PREFIX) ||
				!grouperConfiguration.getGroupTypes().contains(INCLUDE_EXCLUDE_GROUP_TYPE)){
			addMembershipsSimple(groupId, grouperName, membersToAdd);
		}
		else {
			// Add the members to the includes group, then remove them from the excludes.
			addMembershipsSimple(groupId, grouperName + INCLUDE_SUFFIX, membersToAdd);
			removeMembershipsSimple(groupId, grouperName + EXCLUDE_SUFFIX, membersToAdd);
		}
	}

	private void addMembershipsSimple(String groupId, String grouperName, Collection<String> membersToAdd) throws GrouperException{

		// Clean the list of principles/subjects to be added.
		Collection<String> cleanedMembersToAdd = cleanMemberNames(membersToAdd);

		if (!cleanedMembersToAdd.isEmpty()){
			// Each subjectId must have a lookup
			WsSubjectLookup[] subjectLookups = new WsSubjectLookup[membersToAdd.size()];
			int  i = 0;
			for (String subjectId: membersToAdd){
				// TODO - Specify the Grouper subject source in the lookup.
				subjectLookups[i] = new WsSubjectLookup(subjectId, null, null);
				i++;
			}

			WsRestAddMemberRequest addMembers = new WsRestAddMemberRequest();
			// Don't overwrite the entire group membership. just add to it.
			addMembers.setReplaceAllExisting("F");
			addMembers.setSubjectLookups(subjectLookups);

			String urlPath = "/groups/" + grouperName + "/members";
			urlPath = urlPath.replace(":", "%3A");
			// Send the request and parse the result, throwing an exception on failure.
			JSONObject response = post(urlPath, addMembers);
			WsAddMemberResults results = (WsAddMemberResults)JSONObject.toBean(
					response.getJSONObject("WsAddMemberResults"), WsAddMemberResults.class);
			if (!"T".equals(results.getResultMetadata().getSuccess())) {
					throw new GrouperWSException(results);
			}
			log.debug("Success! Added members: Group = {} members = {}",
					grouperName, StringUtils.join(membersToAdd.toArray(), ","));
		}
	}

	/**
	 * @{inheritDoc}
	 */
	public void removeMemberships(String groupId, Collection<String> membersToRemove) throws GrouperException {
		checkGroupId(groupId);

		String grouperName = grouperNameManager.getGrouperName(groupId) + INCLUDE_SUFFIX;
		String membersString = StringUtils.join(membersToRemove, ',');
		log.debug("Removing members: Group = {} members = {}", grouperName, membersString);

		if ( groupId.startsWith(ContactsGrouperNameProviderImpl.CONTACTS_GROUPID_PREFIX) ||
				!grouperConfiguration.getGroupTypes().contains(INCLUDE_EXCLUDE_GROUP_TYPE)){
			removeMembershipsSimple(groupId, grouperName, membersToRemove);
		}
		else {
			removeMembershipsSimple(groupId, grouperName + INCLUDE_SUFFIX, membersToRemove);
			addMembershipsSimple(groupId, grouperName + EXCLUDE_SUFFIX, membersToRemove);
		}
	}

	private void removeMembershipsSimple(String groupId, String grouperName, Collection<String> membersToRemove) throws GrouperException {
		checkGroupId(groupId);

		String membersString = StringUtils.join(membersToRemove, ',');
		log.debug("Removing members: Group = {} members = {}", grouperName, membersString);

		membersToRemove = cleanMemberNames(membersToRemove);

		WsRestDeleteMemberRequest deleteMembers = new WsRestDeleteMemberRequest();
		// Each subjectId must have a lookup
		WsSubjectLookup[] subjectLookups = new WsSubjectLookup[membersToRemove.size()];
		int  i = 0;
		for (String subjectId: membersToRemove){
			subjectLookups[i] = new WsSubjectLookup(subjectId, null, null);
			i++;
		}

		// Delete the members from the _include group
		deleteMembers.setSubjectLookups(subjectLookups);
		String urlPath = "/groups/" + grouperName + "/members";
		urlPath = urlPath.replace(":", "%3A");
		JSONObject response = post(urlPath, deleteMembers);

		WsDeleteMemberResults results = (WsDeleteMemberResults)JSONObject.toBean(
				response.getJSONObject("WsDeleteMemberResults"), WsDeleteMemberResults.class);
		if (!"T".equals(results.getResultMetadata().getSuccess())) {
				throw new GrouperWSException(results);
		}

		log.debug("Success! Removed members: Group = {} members = {}",
				grouperName, membersString);
	}

	private void checkGroupId(String groupId) throws InvalidGroupIdException, GrouperException{
		try {
			Session session = repository.loginAdministrative(grouperConfiguration.getIgnoredUserId());
			AuthorizableManager authorizableManager = session.getAuthorizableManager();
			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);
			session.logout();

			if (!authorizable.isGroup()){
				throw new InvalidGroupIdException(groupId + " is not a group");
			}
		}
		catch (StorageClientException sce) {
			throw new GrouperException("Unable to fetch authorizable for " + groupId);
		}
		catch (AccessDeniedException ade) {
			throw new GrouperException("Unable to fetch authorizable for " + groupId + ". Access Denied.");
		}
	}

	public Collection<String> cleanMemberNames(Collection<String> memberIds) throws GrouperException{
		Collection<String> cleaned = new ArrayList<String>();

		try {
			Session session = repository.loginAdministrative(grouperConfiguration.getIgnoredUserId());
			AuthorizableManager authorizableManager = session.getAuthorizableManager();
			Authorizable authorizable = null;
			for (String memberId: memberIds){
				try {
					authorizable = authorizableManager.findAuthorizable(memberId);
				}
				catch (StorageClientException sce) {
					throw new GrouperException("Unable to fetch authorizable for " + memberId);
				}
				catch (AccessDeniedException ade) {
					throw new GrouperException("Unable to fetch authorizable for " + memberId + ". Access Denied.");
				}

				if (authorizable == null || authorizable.isGroup()){
					log.error("{} is not a valid User id.", memberId);
					continue;
				}
				if (memberId.equals("admin")){
					// Don't bother adding the admin user as a member.
					// It probably doesn't exist in grouper.
					continue;
				}
				cleaned.add(memberId);
			}
			session.logout();
		}
		catch (StorageClientException sce) {
			throw new GrouperException("Unable to fetch authorizable");
		}
		catch (AccessDeniedException ade) {
			throw new GrouperException("Unable to fetch authorizable. Access Denied.");
		}
		return cleaned;
	}

	/**
	 * Issue an HTTP POST to Grouper Web Services
	 *
	 * TODO: Is there a better type for the grouperRequestBean parameter?
	 *
	 * @param grouperRequestBean a Grouper WS bean representing a grouper action
	 * @return the parsed JSON response
	 * @throws HttpException
	 * @throws IOException
	 * @throws GrouperException
	 */
	private JSONObject post(String urlPath, Object grouperRequestBean) throws GrouperException  {
		try {
			// URL e.g. http://localhost:9090/grouper-ws/servicesRest/v1_6_003/...
			HttpClient client = GrouperHttpUtil.getHttpClient(grouperConfiguration);
			String grouperWsRestUrl = grouperConfiguration.getRestWsUrlString() + urlPath;
	        PostMethod method = new PostMethod(grouperWsRestUrl);
	        method.setRequestHeader("Connection", "close");

		    // Encode the request and send it off
		    String requestDocument = GrouperJsonUtil.toJSONString(grouperRequestBean);
		    method.setRequestEntity(new StringRequestEntity(requestDocument, "text/x-json", "UTF-8"));

		    int responseCode = client.executeMethod(method);
		    log.info("POST to {} : {}", grouperWsRestUrl, responseCode);
		    String responseString = IOUtils.toString(method.getResponseBodyAsStream());
		    return JSONObject.fromObject(responseString);
		}
		catch (Exception e) {
			throw new GrouperException(e.getMessage());
		}
	}
}