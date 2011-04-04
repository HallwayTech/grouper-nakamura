package edu.nyu.grouper;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;

import edu.nyu.grouper.api.GrouperIdHelper;
import edu.nyu.grouper.osgi.api.GrouperConfiguration;

/**
 * 
 * Map SakaiOAE groupIds to Grouper group names.
 * 
 * In SakaiOAE each {@link Group} has its own members and a managers group.
 * In Grouper these are located at:
 * some:base:stem:grouperId:members = UNION(inst:grouperId:all, sakai:grouperId:members_sakaioae)
 * some:base:stem:grouperId:managers = UNION(inst:grouperId:instructors, sakai:grouperId:managers_sakaioae)
 * 
 * Each of those two groups contains the union of the membership of two or more subgroups.
 * One or more those groups is managed by the institution. They should reside in a 
 * different stem where SakaiOAE doesn't have write access.
 * 
 * The other subgroup of the members and manager groups is where Sakai attempts
 * to reads and write.
 * some:base:stem:grouperId:members_sakaioae
 * some:base:stem:grouperId:managers_sakaioae
 *
 */
@Service
@Component
public class AggregateGroupsGrouperIdHelperImpl implements GrouperIdHelper {
	
	@Reference
	protected GrouperConfiguration grouperConfiguration;

	public String getGrouperName(String groupId) {
		if (groupId == null){
			return null;
		}
		return getFullStem(groupId) + ":" + getGrouperExtension(groupId);
	}

	/**
	 * Get the extension for this group in Grouper.
	 * The extension is the last component in a Grouper name
	 */
	public String getGrouperExtension(String groupId) {
		if (groupId == null){
			return null;
		}
		String extension;
		if(isManagersGroup(groupId)){
			extension = "managers" + grouperConfiguration.getSuffix();
		}
		else {
			extension = "members" + grouperConfiguration.getSuffix();
		}
		return extension;
	}
	
	/**
	 * @return the stem for this SakaiOAE group in Grouper.
	 * 
	 * groupId == some_thing => base:stem:some:thing
	 * groupId == some_thing-managers => base:stem:some:thing
	 */
	public String getFullStem(String groupId){
		if (groupId == null){
			return null;
		}
		int indexOfManagers = groupId.indexOf("-managers");
		if (indexOfManagers != -1){
			groupId = groupId.substring(0, indexOfManagers);
		}
		return grouperConfiguration.getBaseStem() + ":" + getPartialStem(groupId);
	}
	
	/**
	 * Convert the groupId into a stem.
	 * this_group_name => this:group:name
	 * 
	 * @param groupId the sakaiOAE groupId
	 * @return a partial stem for the corresponding grouper Group
	 */
	private String getPartialStem(String groupId){
		if (groupId == null){
			return null;
		}
		return groupId.replaceAll("_", ":");
	}
	
	/**
	 * @param groupId
	 * @return whether or not this is a managers group for another group
	 */
	private boolean isManagersGroup(String groupId){
		return groupId.endsWith("-managers");
	}
	
	public void bindGrouperConfiguration(GrouperConfiguration config){
		this.grouperConfiguration = config;
	}
}