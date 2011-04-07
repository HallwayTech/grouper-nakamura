package edu.nyu.grouper.util;

import edu.nyu.grouper.util.api.GroupIdAdapter;

public class AggregateGroupIdAdapter extends BaseNakamuraGroupIdAdapter implements GroupIdAdapter {
	
	private String basestem;
	
	public AggregateGroupIdAdapter(String basestem){
		super(basestem);
		this.basestem = basestem;
	}

	/**
	 * base:stem:some:group:members => some_group
	 * base:stem:some:group:managers => some_group-managers
	 * base:stem:group:members => group
	 * base:stem:group:managers => group-managers
	 */
	public String getNakamuraGroupId(String grouperName) {
		int lastColonPosition = grouperName.lastIndexOf(":");		
		
		// Remove the base stem and the extension
		//  base:stem:some:group:managers => some_group
		String middle = stripBaseStem(grouperName.substring(0, lastColonPosition));
		middle = middle.replaceAll(":", "_");
		
		// The extension is the members or managers group
		// base:stem:some:group:managers => managers
		// base:stem:some:group:members => members
		String groupId = grouperName.substring(lastColonPosition + 1);

		if(groupId.equals("managers")){
			groupId = middle + "-managers";
		}
		else {
			groupId = middle;
		}
		return groupId;
	}

}