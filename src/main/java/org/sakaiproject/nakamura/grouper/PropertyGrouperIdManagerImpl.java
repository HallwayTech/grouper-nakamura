package org.sakaiproject.nakamura.grouper;

import org.apache.felix.scr.annotations.Reference;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
import org.sakaiproject.nakamura.grouper.api.GrouperIdManager;
import org.sakaiproject.nakamura.grouper.api.GrouperManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyGrouperIdManagerImpl implements GrouperIdManager {

	private static final Logger log = LoggerFactory.getLogger(PropertyGrouperIdManagerImpl.class);
	
	@Reference
	protected GrouperConfiguration grouperConfiguration;
	
	@Reference
	protected Repository repository;
	
	public static String[] SPECIAL_GROUP_SUFFIXES = new String[] {"-managers", "-ta", "-lecturer"};

	@Override
	public String getGrouperName(String groupId) {
		return getProperty(groupId, GrouperManager.GROUPER_NAME_PROP);
	}
	
	private String getProperty(String groupId, String propertyName){
		String propValue = null;
		Authorizable authorizable = null;
		AuthorizableManager authorizableManager = null;
		Session session = null;
		try {
			session = repository.loginAdministrative();
			authorizableManager = session.getAuthorizableManager();
			authorizable = authorizableManager.findAuthorizable(groupId);
			
			if (authorizable != null){
				propValue = (String)authorizable.getProperty(propertyName);
			}
			session.logout();
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
		finally {
			try {
				if (session != null){
					session.logout();
				}
			} catch (ClientPoolException e) {
				log.error("Error closing repository session.", e.getMessage());
			}
			finally {
				session = null;
			}
		}
		return propValue;
	}

}
