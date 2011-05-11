package edu.nyu.grouper;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nyu.grouper.api.GrouperConfiguration;
import edu.nyu.grouper.api.GrouperIdHelper;

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
	
	private static final Logger log = LoggerFactory.getLogger(AggregateGroupsGrouperIdHelperImpl.class);
	
	@Reference
	protected GrouperConfiguration grouperConfiguration;
	
	@Reference
	protected Repository repository;
	
	private AuthorizableManager authorizableManager;
	
	public static String[] SPECIAL_GROUP_SUFFIXES = new String[] {"-managers", "-ta"};

	@Activate
	public void activate(Map<?,?> props) throws ClientPoolException, StorageClientException, AccessDeniedException{
		authorizableManager = repository.loginAdministrative().getAuthorizableManager();
	}

	public String getGrouperName(String groupId) {
		if (groupId == null){
			return null;
		}
		String grouperName = null;
		Authorizable authorizable = null;
		try {
			if (authorizableManager != null){
				authorizable = authorizableManager.findAuthorizable(groupId);
			}
			if (authorizable != null){
				grouperName = (String)authorizable.getProperty("grouper:name");
			}
		} 
		catch (AccessDeniedException e) {
			if (log.isErrorEnabled()){
				log.error("Error finding authorizable for {}. Access denied", groupId, e.getMessage());
			}
		} 
		catch (StorageClientException e) {
			if (log.isErrorEnabled()){
				log.error("Error finding authorizable for {}. StorageClientException", groupId, e.getMessage());
			}
		}
		
		if (grouperName == null){
			grouperName = getFullStem(groupId) + ":" + getGrouperExtension(groupId);
		}
		return grouperName;
	}

	/**
	 * Get the extension for this group in Grouper.
	 * The extension is the last component in a Grouper name
	 */
	public String getGrouperExtension(String groupId) {
		if (groupId == null){
			return null;
		}
		String extension = "members";
		for (String suffix: SPECIAL_GROUP_SUFFIXES) {
			int indexOfSuffix= groupId.indexOf(suffix);
			if (indexOfSuffix != -1){
				extension = groupId.substring(indexOfSuffix + 1);
			}
		}
		extension += grouperConfiguration.getSuffix();

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
		for (String suffix: SPECIAL_GROUP_SUFFIXES){
			int indexOfSuffix = groupId.indexOf(suffix);
			if (indexOfSuffix != -1){
				groupId = groupId.substring(0, indexOfSuffix);
			}
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

	public void bindGrouperConfiguration(GrouperConfiguration config){
		this.grouperConfiguration = config;
	}
}