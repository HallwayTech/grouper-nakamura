package edu.nyu.grouper.api;

import java.util.Collection;

import org.osgi.service.event.Event;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;

import edu.nyu.grouper.GrouperException;

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
	public void createGroup(String groupId) throws GrouperException;

	/**
	 * Delete a Grouper group because this group is being deleted by nakmura.
	 * @param groupId the id of the {@link Authorizable} for this group
	 */
	public void deleteGroup(String groupId) throws GrouperException;

	/**
	 * Add members to a Grouper group.
	 * @param groupId the id of the {@link Authorizable} for this group.
	 * @param membersToAdd
	 */
	public void addMemberships(String groupId, Collection<String> membersToAdd) throws GrouperException;
	
	/**
	 * Add members to a Grouper group.
	 * @param groupId the id of the {@link Authorizable} for this group.
	 * @param membersToRemove
	 */
	public void removeMemberships(String groupId, Collection<String> membersToRemove) throws GrouperException;

	/**
	 * Update properties in Grouper
	 * @param groupId the id of the {@link Authorizable} for this group.
	 * @param event an OSGi event. 
	 */
	public void updateGroup(String groupId, Event event) throws GrouperException;

}
