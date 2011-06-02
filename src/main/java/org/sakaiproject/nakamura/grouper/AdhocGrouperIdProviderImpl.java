/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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

/**
 * 
 *
 */
public class AdhocGrouperIdProviderImpl implements GrouperIdManager {

	private static final Logger log = LoggerFactory.getLogger(AdhocGrouperIdProviderImpl.class);

	@Reference
	protected GrouperConfiguration config;

	@Reference
	protected Repository repository;

	@Override
	public String getGrouperName(String groupId) {
		
		if (groupId == null){
			return null;
		}
		
		// This group has already been assigned a group in grouper.
		String grouperName = getProperty(groupId, GrouperManager.GROUPER_NAME_PROP);
		if (grouperName != null){
			return null;
		}
		
		StringBuilder gn = new StringBuilder(config.getBaseStem("group"));
		gn.append(":");
		gn.append(groupId.charAt(0));
		gn.append(":");
		gn.append(groupId.substring(0,2));
		gn.append(":");
		gn.append(BaseGrouperIdProvider.getGrouperLastStem(groupId, config));
		gn.append(":");
		gn.append(BaseGrouperIdProvider.getGrouperExtension(groupId, config));
		grouperName = gn.toString();
		return grouperName;
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

	public void bindGrouperConfiguration(GrouperConfiguration gconfig) {
		this.config = gconfig;
	}
}
