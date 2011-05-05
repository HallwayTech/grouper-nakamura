package edu.nyu.grouper;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import edu.nyu.grouper.api.GrouperIdHelper;
import edu.nyu.grouper.api.GrouperConfiguration;

/**
 * @see GrouperIdHelper
 */
@Service
@Component(enabled=false)
public class SimpleGrouperIdHelperImpl implements GrouperIdHelper{

	@Reference
	protected GrouperConfiguration grouperConfiguration;
	
	/**
	 * @see SimpleGrouperIdHelperImpl#getFullGrouperName(String, String)
	 * examples:
	 * getFullGrouperName(base:stem:, some_class_id) => base:stem:some:group:class:id
	 * getFullGrouperName(nyu:apps:sakai3:, cs_101_sp11) => nyu:apps:sakai3:cs:101:sp11
	 */
	public String getGrouperName(String groupId) {
		return grouperConfiguration.getBaseStem() + ":" + groupId.replaceAll("_", ":");
	}
	
	/**
	 * @see SimpleGrouperIdHelperImpl#getGrouperExtension(String, String)
	 * examples:
	 * getName(id) => id
	 * getName(cs_101_sp11) => sp11
	 */
	public String getGrouperExtension(String groupId) {
		String name = getGrouperName(groupId);
		return name.substring(name.lastIndexOf(":") + 1);
	}
}
