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
package org.sakaiproject.nakamura.grouper.name.api;

/**
 * Service class to translate ids to Grouper ids.
 * The GrouperNameManager will hold an ordered list of GrouperNameProvider so
 * it can delegate the grouper name resolution.
 */
public interface GrouperNameManager {
	/**
	 * @param baseStem the folder in Grouper for sakai3
	 * @param the authorizableId of the Group
	 * @return the fully qualified name of this group in Grouper
	 */
	public String getGrouperName(String groupId);
}
