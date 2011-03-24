package edu.nyu.grouper.util;

import edu.nyu.grouper.xmpp.api.GroupIdAdapter;

/**
 * 
 * @author froese
 *
 */
public class BaseNakamuraGroupIdAdapter implements GroupIdAdapter {
	
	private String basestem;
	
	public BaseNakamuraGroupIdAdapter(String basestem){
		this.basestem = basestem;
	}

	public String getNakamuraName(String grouperName) {
		return stripBaseStem(grouperName).replaceAll(":", "_");
	}

	public String getGrouperFullName(String nakamuraGroupName) {
		String grouperFullName = nakamuraGroupName.replaceAll("_", ":");
		if(basestem != null && ! "".equals(basestem)){
			grouperFullName = basestem + ":" + grouperFullName;
		}
		return grouperFullName;
	}
	
	private String stripBaseStem(String grouperName){
		if (grouperName.startsWith(basestem + ":")){
			grouperName = grouperName.substring(basestem.length() + 1);
		}
		return grouperName;
	}

}
