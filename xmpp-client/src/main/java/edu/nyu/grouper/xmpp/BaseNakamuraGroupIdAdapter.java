package edu.nyu.grouper.xmpp;

import edu.nyu.grouper.xmpp.api.GroupIdAdapter;

/**
 * 
 * @author froese
 *
 */
public class BaseNakamuraGroupIdAdapter implements GroupIdAdapter {

	public String getNakamuraName(String fullGrouperGroupName) {
		return fullGrouperGroupName.replaceAll(":", "_");
	}

	public String getGrouperFullName(String nakamuraGroupName) {
		return nakamuraGroupName.replaceAll("_", ":");		
	}

}
