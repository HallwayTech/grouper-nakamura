package org.sakaiproject.nakamura.grouper.util;

import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.grouper.name.ContactsGrouperNameProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupUtil {
	
	private static final Logger log = LoggerFactory.getLogger(GroupUtil.class);

	/**
	 * Is this the group part of a Simple Group, either the
	 * parent or one of the pseudo groups. 
	 * @param g
	 * @return if g is part of a simple group
	 */
	public static boolean isSimpleGroup(Group g, Session session){
		boolean simple = false;
		try {
			if (g.getId().endsWith("-manager") ||
					session.getAuthorizableManager().findAuthorizable(g.getId() + "-manager") != null){
				simple = true;
			}
		}
		catch (Exception e){
			log.error("Error looking up {} in sparsemap.", g.getId());
		}
		return simple;
	}
	
	/**
	 * Is this the group part of a Course Group, either the
	 * parent or one of the pseudo groups. 
	 * @param g
	 * @return if g is part of a course group
	 */
	public static boolean isCourseGroup(Group g, Session session){
		return (!isSimpleGroup(g, session)) && (!isContactsGroup(g.getId()));
	}

	public static boolean isContactsGroup(String groupId) {
		return groupId.startsWith(ContactsGrouperNameProviderImpl.CONTACTS_GROUPID_PREFIX);
	}

}
