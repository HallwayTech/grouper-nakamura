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

import java.util.List;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
import org.sakaiproject.nakamura.grouper.api.GrouperNameManager;
import org.sakaiproject.nakamura.grouper.api.GrouperNameProvider;

public class GrouperNameManagerImpl implements GrouperNameManager { 
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY_MULTIPLE)
	protected List<GrouperNameProvider> idProviders;

	@Reference
	protected GrouperConfiguration config;

	@Override
	public String getGrouperName(String groupId) {
		String gn = null;
		for (GrouperNameProvider gnp: idProviders){
			gn = gnp.getGrouperName(groupId);
			if (gn != null){
				return gn;
			}
		}
		return null;
	}

	/**
	 * Get the extension for this group in Grouper.
	 * The extension is the last component in a Grouper name
	 */
	public String getGrouperExtension(String groupId) {
		if (groupId == null){
			return null;
		}
		String extension = "members";
		for (String suffix: config.getSpecialGroupSuffixes() ) {
			int indexOfSuffix= groupId.indexOf(suffix);
			if (indexOfSuffix != -1){
				extension = groupId.substring(indexOfSuffix + 1);
			}
		}
		return extension;
	}

}
