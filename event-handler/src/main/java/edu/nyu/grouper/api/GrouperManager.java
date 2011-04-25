package edu.nyu.grouper.api;

import java.util.Collection;

import org.osgi.service.event.Event;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;

/**
 * Manage the interaction with a Grouper WS server.
 */
public interface GrouperManager {

	/**
	 * Create a Grouper group for a nakamura group
	 * @param groupId the id of the {@link Authorizable} for this group.
	 */
	public void createGroup(String groupId);

	/**
	 * Delete a Grouper group because this group is being deleted by nakmura.
	 * @param groupId the id of the {@link Authorizable} for this group
	 */
	public void deleteGroup(String groupId);

	/**
	 * Add members to a Grouper group.
	 * @param groupId the id of the {@link Authorizable} for this group.
	 * @param membersToAdd
	 */
	public void addMemberships(String groupId, Collection<String> membersToAdd);
	
	/**
	 * Add members to a Grouper group.
	 * @param groupId the id of the {@link Authorizable} for this group.
	 * @param membersToRemove
	 */
	public void removeMemberships(String groupId, Collection<String> membersToRemove);

	/**
	 * Update properties in Grouper
	 * @param groupId the id of the {@link Authorizable} for this group.
	 * @param event an OSGi event. 
	 */
	public void updateGroup(String groupId, Event event);

}
