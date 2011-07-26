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
package org.sakaiproject.nakamura.grouper.name;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
import org.sakaiproject.nakamura.grouper.name.api.GrouperNameManager;
import org.sakaiproject.nakamura.grouper.name.api.GrouperNameProvider;
import org.sakaiproject.nakamura.grouper.util.GroupUtil;
import org.sakaiproject.nakamura.util.osgi.AbstractOrderedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate a grouper name based on a Sakai OAE groupId
 *
 * This class holds an ordered {@link List} of {@link GrouperNameProvider}s.
 * The first one to return an answer for the group will be used.
 *
 */
@Service
@Component
public class GrouperNameManagerImpl extends AbstractOrderedService<GrouperNameProvider> implements GrouperNameManager {

	private static Logger log = LoggerFactory.getLogger(GrouperNameManagerImpl.class);

	@Reference(cardinality=ReferenceCardinality.MANDATORY_MULTIPLE)
	protected GrouperNameProvider[] orderedServices = new GrouperNameProvider[0];

	@Reference
	protected GrouperConfiguration config;

	@Reference Repository repository;

	@Override
	public String getGrouperName(String groupId) {
		String grouperName = null;
		for (GrouperNameProvider gnp: orderedServices){
			grouperName = gnp.getGrouperName(groupId);
			if (grouperName != null){
				break;
			}
		}

		try {
			Session session = repository.loginAdministrative(config.getIgnoredUserId());
			Group g = (Group)session.getAuthorizableManager().findAuthorizable(groupId);
			if (GroupUtil.isContactsGroup(g.getId()) && !grouperName.startsWith(config.getContactsStem())){
				grouperName = config.getContactsStem() + ":" + grouperName;
			}
			else if (GroupUtil.isSimpleGroup(g, session) && !grouperName.startsWith(config.getSimpleGroupsStem())){
				grouperName = config.getSimpleGroupsStem() + ":" + grouperName;
			}
			else if (GroupUtil.isCourseGroup(g, session)&& !grouperName.startsWith(config.getCoursesStem())){
				grouperName = config.getCoursesStem() + ":" + grouperName;
			}
			session.logout();
		} catch (ClientPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("Resolved groupId {} to grouperName {}", groupId, grouperName);

		return grouperName;
	}

	/**
	 * Get the extension for this group in Grouper.
	 * The extension is the last component in a Grouper name
	 */
	public String getGrouperExtension(String groupId) {
		return BaseGrouperNameProvider.getGrouperExtension(groupId, config);
	}

	/*
	 * below here is just boilerplate for the ordered list of providers.
	 * Providers are sorted by their service.ranking property in ascending order.
	 * MAKE SURE THE PROPERTY VALUE IS AN INTEGER! use intValue
	 */

	protected void bindGrouperNameProvider(GrouperNameProvider gnp, Map<String, Object> properties) {
		addService(gnp, properties);
	}

	protected void unbindGrouperNameProvider(GrouperNameProvider gnp, Map<String, Object> properties) {
		removeService(gnp, properties);
	}

	protected void saveArray(List<GrouperNameProvider> gnpList) {
		orderedServices = gnpList.toArray(new GrouperNameProvider[gnpList.size()]);
	}

	@Override
	protected Comparator<? super GrouperNameProvider> getComparator(
			final Map<GrouperNameProvider, Map<String, Object>> propertiesMap) {

		return new Comparator<GrouperNameProvider>() {
			public int compare(GrouperNameProvider o1, GrouperNameProvider o2) {
				Map<String, Object> props1 = propertiesMap.get(o1);
				Map<String, Object> props2 = propertiesMap.get(o2);
				return OsgiUtil.getComparableForServiceRanking(props1).compareTo(props2);
			}
		};
	}
}
