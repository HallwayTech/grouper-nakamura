package org.sakaiproject.nakamura.grouper;

import java.util.List;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
import org.sakaiproject.nakamura.grouper.api.GrouperIdManager;
import org.sakaiproject.nakamura.grouper.api.GrouperIdProvider;

public class GrouperIdManagerImpl implements GrouperIdManager { 
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY_MULTIPLE)
	protected List<GrouperIdProvider> idProviders;

	@Reference
	protected GrouperConfiguration config;

	@Override
	public String getGrouperName(String groupId) {
		String gn = null;
		for (GrouperIdProvider gip: idProviders){
			gn = gip.getGrouperName(groupId);
			if (gn != null){
				return gn;
			}
		}
		return null;
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
		for (String suffix: config.getSpecialGroupSuffixes() ) {
			int indexOfSuffix= groupId.indexOf(suffix);
			if (indexOfSuffix != -1){
				extension = groupId.substring(indexOfSuffix + 1);
			}
		}
		return extension;
	}

}
