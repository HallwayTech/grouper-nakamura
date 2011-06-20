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

import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;

public abstract class BaseGrouperNameProvider {

	/**
	 * Return the extension for this group in grouper.
	 * 
	 * examples:
	 * group0 => member
	 * group0-managers => manager
	 * 
	 * course0 => member
	 * course0-ta => ta
	 * 
	 * @param groupId the id of this group in sakaiOAE.
	 * @param config the grouper configuration
	 * @return the extension for the group in grouper
	 */
	public static String getGrouperExtension(String groupId, GrouperConfiguration config) {
		if (groupId == null){
			return null;
		}

		if (groupId.startsWith("g-contacts")){
			return "contacts";
		}

		String extension = "member";
		for (String suffix: config.getPseudoGroupSuffixes()){
			if (groupId.endsWith(suffix)){
				extension = suffix.substring(1);
			}
		}
		return extension;
	}
	
	public static String getGrouperLastStem(String groupId, GrouperConfiguration config) {
		if (groupId == null){
			return null;
		}

		int contactGroupIndex = groupId.indexOf("g-contacts"); 
		if (contactGroupIndex != -1){
			return groupId.substring(contactGroupIndex + 11);
		}

		String stem = groupId;
		for (String suffix: config.getPseudoGroupSuffixes()){
			if (groupId.endsWith(suffix)){
				stem = groupId.substring(0, groupId.lastIndexOf(suffix));
			}
		}
		return stem;
	}
}
