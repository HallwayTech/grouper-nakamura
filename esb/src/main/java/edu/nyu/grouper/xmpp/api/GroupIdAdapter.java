package edu.nyu.grouper.xmpp.api;

/**
 * A strategy to map names from grouper to Nakamura.
 */
public interface GroupIdAdapter {

	/**
	 * Get the ID of a group in Nakamura given the full group name from Grouper.
	 * @param grouperGroupId the full group id in grouper ex: stem1:stem2:groupName
	 * @return the group ID in nakamura ex: stem1_stem2_groupName
	 */
	public String getNakamuraName(String grouperGroupId);

	/**
	 * Get the ID of a group in Grouper given the full group name from Nakamura
	 * @param nakamuraGroupId the group ID in nakamura ex: stem1_stem2_groupName
	 * @return the group ID in Grouper ex: stem1:stem2:groupName
	 */
	public String getGrouperFullName(String nakamuraGroupId);

}