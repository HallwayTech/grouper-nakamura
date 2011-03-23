package edu.nyu.grouper;

import edu.nyu.grouper.api.GrouperIdHelper;

/**
 * @see GrouperIdHelper
 */
public class BaseGrouperIdHelper implements GrouperIdHelper{

	/**
	 * @see BaseGrouperIdHelper#getFullGrouperName(String, String)
	 */
	public String getFullGrouperName(String baseStem, String groupId) {
		return baseStem + ":" + groupId.replaceAll("_", ":");
	}

	/**
	 * @see BaseGrouperIdHelper#getFullStem(String, String)
	 */
	public String getFullStem(String baseStem, String groupId) {
		String full = getFullGrouperName(baseStem, groupId);
		return full.substring(0, full.lastIndexOf(":") - 1);
	}
	
	/**
	 * @see BaseGrouperIdHelper#getName(String, String)
	 */
	public String getName(String groupId) {
		groupId = groupId.replaceAll("_", ";");
		return groupId.substring(groupId.lastIndexOf(":") + 1);
	}
	
}
