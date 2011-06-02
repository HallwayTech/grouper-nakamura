package org.sakaiproject.nakamura.grouper;

import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;

public abstract class BaseGrouperIdProvider {

	/**
	 * Return the extension for this group in grouper.
	 * 
	 * examples:
	 * group0 => members
	 * group0-managers => managers
	 * 
	 * course0 => members
	 * course0-ta => ta
	 * 
	 * @param groupId the id of this group in sakaiOAE.
	 * @param config the grouper configuration
	 * @return the extension for the group in grouper
	 */
	public static String getGrouperExtension(String groupId, GrouperConfiguration config) {
		if (groupId == null){
			return null;
		}
		String extension = "members";
		for(String suffix: config.getSpecialGroupSuffixes()){
			if (groupId.endsWith(suffix)){
				extension = suffix.substring(1);
			}
		}
		return extension;
	}
}
