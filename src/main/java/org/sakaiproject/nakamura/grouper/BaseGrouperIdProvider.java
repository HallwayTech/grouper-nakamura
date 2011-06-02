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

public abstract class BaseGrouperIdProvider {

	/**
	 * Return the extension for this group in grouper.
	 * 
	 * examples:
	 * group0 => members
	 * group0-managers => managers
	 * 
	 * course0 => members
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
		String extension = "members";
		for (String suffix: config.getSpecialGroupSuffixes()){
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
		String stem = groupId;
		for (String suffix: config.getSpecialGroupSuffixes()){
			if (groupId.endsWith(suffix)){
				stem = groupId.substring(0, groupId.lastIndexOf(suffix));
			}
		}
		return stem;
	}
}
