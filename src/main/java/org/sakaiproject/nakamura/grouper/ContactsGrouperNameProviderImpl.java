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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
import org.sakaiproject.nakamura.grouper.api.GrouperNameManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide grouper names based on the following structure:
 * 
 * g-contacts-user1 => base:stem:users:user1:contacts
 * 
 * my-group-name => null
 * my-group-name-managers => null
 */
@Service
@Component
@Properties(value = { 
		@Property(name = "service.ranking", value = "10") 
})
public class ContactsGrouperNameProviderImpl implements GrouperNameManager {

	private static final Logger log = LoggerFactory.getLogger(ContactsGrouperNameProviderImpl.class);

	private static final String CONTACTS_GROUPID_PREFIX = "g-contacts";

	@Reference
	protected GrouperConfiguration config;

	@Override
	public String getGrouperName(String groupId) {

		if (groupId == null || !groupId.startsWith(CONTACTS_GROUPID_PREFIX)){
			return null;
		}

		StringBuilder gn = new StringBuilder(config.getBaseStem("contacts"));
		gn.append(":");
		gn.append(BaseGrouperNameProvider.getGrouperLastStem(groupId, config));
		gn.append(":");
		gn.append(BaseGrouperNameProvider.getGrouperExtension(groupId, config));

		String grouperName = gn.toString();
		log.info("groupId: {} => grouperName: {}", groupId, grouperName);
		return grouperName;
	}

	public void bindGrouperConfiguration(GrouperConfiguration gconfig) {
		this.config = gconfig;
	}
}
