package edu.nyu.grouper.api;

/**
 * Utility class to translate ids to Grouper ids
 * First pass at converting grouper groups to Nakamura groups by the name.
 * 
 * groupId is the authorizableId if of the form some_thing_name1
 * baseStem is of the form edu:apps:sakai3
 * 
 * The full name will be edu:apps:sakai3:some:thing:name1
 * The full stem will be edu:apps:sakai3:some:thing
 * The name will be name1
 */
public interface GrouperIdHelper {

	/**
	 * @param baseStem the folder in Grouper for sakai3
	 * @param the authorizableId of the Group
	 * @return the fully qualified name of this group in Grouper
	 * 
	 * examples:
	 * getFullGrouperName(base:stem:, some_class_id) => base:stem:some:group:class:id
	 * getFullGrouperName(nyu:apps:sakai3:, cs_101_sp11) => nyu:apps:sakai3:cs:101:sp11
	 */
	public String getFullGrouperName(String baseStem, String groupId);
	
	/**
	 * @param baseStem the folder in Grouper for sakai3
	 * @param the authorizableId of the Group
	 * @return the full stem representing the folder this group is in Grouper
	 * 
	 * examples:
	 * getFullStem(base:stem:, some_class_id) => base:stem:some:group:id
	 * getFullStem(nyu:apps:sakai3:, cs_101_sp11) => nyu:apps:sakai3:cs:101
	 */
	public String getFullStem(String baseStem, String groupId);
	
	/**
	 * @param baseStem the folder in Grouper for sakai3
	 * @param the authorizableId of the Group
	 * @return the full stem representing the Id of this group in Grouper
	 * 
	 * examples:
	 * getName(id) => id
	 * getName(cs_101_sp11) => sp11 
	 */
	public String getName(String groupId);
}
