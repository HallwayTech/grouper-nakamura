package org.sakaiproject.nakamura.grouper.authorizable;

import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManagerPlugin;
import org.sakaiproject.nakamura.grouper.api.GrouperWSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrouperAuthorizableManagerPlugin implements
		AuthorizableManagerPlugin {
	
	private static final Logger log = LoggerFactory.getLogger(GrouperAuthorizableManagerPlugin.class);
	private final GrouperAuthorizableManagerPluginFactory factory;
	private GrouperWSConfiguration grouperConfig;
	
	public GrouperAuthorizableManagerPlugin(GrouperAuthorizableManagerPluginFactory factory,
											GrouperWSConfiguration grouperConfig) {
		this.factory = factory;
		this.grouperConfig = grouperConfig;
	}

	public Authorizable findAuthorizable(String authorizableId)
			throws AccessDeniedException, StorageClientException {
		log.debug("findAuthorizable called for {} ", authorizableId);
		return null;
	}

	public void updateAuthorizable(Authorizable authorizable)
			throws AccessDeniedException, StorageClientException {
		log.debug("updateAuthorizable called for {}", authorizable.getId());
	}

	public boolean createGroup(String groupId, String groupName,
			Map<String, Object> properties) throws AccessDeniedException,
			StorageClientException {
		log.debug("createGroup called for {} : {} ", groupId, groupName);
		return false;
	}

	public boolean createUser(String userId, String userName, String password,
			Map<String, Object> properties) throws AccessDeniedException,
			StorageClientException {
		log.debug("createUser called for {} ", userName);
		return false;
	}

	public void delete(String authorizableId) throws AccessDeniedException,
			StorageClientException {
		log.debug("delete called for {} ", authorizableId);
	}

	public void changePassword(Authorizable authorizable, String password,
			String oldPassword) throws StorageClientException,
			AccessDeniedException {
		log.debug("changePassword called for {} ", authorizable);
	}

	public Iterator<Authorizable> findAuthorizable(String propertyName,
			String value, Class<? extends Authorizable> authorizableType)
			throws StorageClientException {
		log.debug("findAuthorizable(propertyName... called for {} = {}", propertyName, value);
		return null;
	}

	public boolean handles(String authorizableId) {
		// TODO - Add a way to configure this.
		// Regex? How do we deal with arbitrary contraints?
		// Length, startswith, endswith
		return true;
	}
	
	public void close(){
		this.factory.removePlugin(this);
	}

	public void setGrouperConfig(GrouperWSConfiguration gc){
		this.grouperConfig = gc;
	}
}