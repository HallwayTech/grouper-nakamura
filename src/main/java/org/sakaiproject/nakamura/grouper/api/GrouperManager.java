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
package org.sakaiproject.nakamura.grouper.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.grouper.exception.GrouperException;

/**
 * Manage the interaction with a Grouper WS server.
 */
public interface GrouperManager {
	
	static String GROUPER_NAME_PROP = "grouper:name";

	/**
	 * Create a Grouper group for a nakamura group
	 * @param groupId the id of the {@link Authorizable} for this group.
	 * @return true if the group was created in Grouper.
	 */
	public void createGroup(String groupId, Set<String> groupTypes) throws GrouperException;

	/**
	 * Delete a Grouper group because this group is being deleted by nakmura.
	 * @param groupId the id of the {@link Authorizable} for this group
	 */
	public void deleteGroup(String groupId) throws GrouperException;

	/**
	 * Delete a group from Grouper.
	 * @param groupId the id of the {@link Authorizable} for this group
	 * @param attributes the properties of this group.
	 * @throws GrouperException
	 */
	public void deleteGroup(String groupId, Map<String, Object> attributes) throws GrouperException;

	/**
	 * Add members to a Grouper group.
	 * @param groupId the id of the {@link Authorizable} for this group.
	 * @param membersToAdd the member id's to add to this group.
	 */
	public void addMemberships(String groupId, Collection<String> membersToAdd) throws GrouperException;

	/**
	 * Add members to a Grouper group.
	 * @param groupId the id of the {@link Authorizable} for this group.
	 * @param membersToRemove the member id's to remove from this group.
	 */
	public void removeMemberships(String groupId, Collection<String> membersToRemove) throws GrouperException;

}
