package edu.nyu.grouper.api;

/**
 * Utility class to translate ids to Grouper ids
 * First pass at converting grouper groups to Nakamura groups by the name.
 * 
 * groupId is the authorizableId if of the form some_thing_name1
 * baseStem is of the form edu:apps:sakai3
 */
public interface GrouperIdHelper {

	/**
	 * @param baseStem the folder in Grouper for sakai3
	 * @param the authorizableId of the Group
	 * @return the fully qualified name of this group in Grouper
	 */
	public String getGrouperName(String groupId);
	
	/**
	 * @param groupId
	 * @return the Grouper group extension
	 */
	public String getGrouperExtension(String groupId);
}
