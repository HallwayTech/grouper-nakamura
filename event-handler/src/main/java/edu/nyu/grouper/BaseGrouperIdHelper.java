package edu.nyu.grouper;

import edu.nyu.grouper.api.GrouperIdHelper;

/**
 * @see GrouperIdHelper
 */
public class BaseGrouperIdHelper implements GrouperIdHelper{

	/**
	 * @see BaseGrouperIdHelper#getFullGrouperName(String, String)
	 * examples:
	 * getFullGrouperName(base:stem:, some_class_id) => base:stem:some:group:class:id
	 * getFullGrouperName(nyu:apps:sakai3:, cs_101_sp11) => nyu:apps:sakai3:cs:101:sp11
	 */
	public String getFullGrouperName(String baseStem, String groupId) {
		return baseStem + ":" + groupId.replaceAll("_", ":");
	}

	/**
	 * @see BaseGrouperIdHelper#getFullStem(String, String)
	 * examples:
	 * getFullStem(base:stem:, some_class_id) => base:stem:some:group:id
	 * getFullStem(nyu:apps:sakai3:, cs_101_sp11) => nyu:apps:sakai3:cs:101
	 */
	public String getFullStem(String baseStem, String groupId) {
		String full = getFullGrouperName(baseStem, groupId);
		return full.substring(0, full.lastIndexOf(":") - 1);
	}
	
	/**
	 * @see BaseGrouperIdHelper#getName(String, String)
	 * examples:
	 * getName(id) => id
	 * getName(cs_101_sp11) => sp11
	 */
	public String getName(String groupId) {
		groupId = groupId.replaceAll("_", ";");
		return groupId.substring(groupId.lastIndexOf(":") + 1);
	}
}
