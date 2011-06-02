package org.sakaiproject.nakamura.grouper.api;

/**
 * This API exists to separate the services providing ids from the one 
 * @author froese
 *
 */
public interface GrouperIdProvider {
	/**
	 * @param baseStem the folder in Grouper for sakai3
	 * @param the authorizableId of the Group
	 * @return the fully qualified name of this group in Grouper
	 */
	public String getGrouperName(String groupId);
}
