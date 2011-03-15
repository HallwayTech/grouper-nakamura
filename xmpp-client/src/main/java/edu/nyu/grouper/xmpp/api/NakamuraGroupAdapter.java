package edu.nyu.grouper.xmpp.api;

import edu.nyu.grouper.xmpp.exceptions.GroupModificationException;

public interface NakamuraGroupAdapter {
	
	/**
	 * Create a group.
	 * @param groupId
	 * @throws GroupModificationException
	 */
	public void createGroup(String groupId, String groupExtension) throws GroupModificationException;
	
	/**
	 * Delete a group.
	 * @param groupId
	 * @throws GroupModificationException
	 */
	public void deleteGroup(String groupId, String groupExtension) throws GroupModificationException;
	
	/**
	 * Add a subject to a group.
	 * @param groupId
	 * @param subject
	 * @throws GroupModificationException
	 */
	public void addMembership(String groupId, String groupExtension, String subjectId) throws GroupModificationException;
	
	/**
	 * Remove a subject from a group.
	 * @param groupId
	 * @param subject
	 * @throws GroupModificationException
	 */
	public void deleteMembership(String groupId, String groupExtension, String subjectId) throws GroupModificationException;
}
