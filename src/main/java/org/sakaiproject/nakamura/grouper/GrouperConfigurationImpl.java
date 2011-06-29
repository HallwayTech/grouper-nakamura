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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.cm.ConfigurationException;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component(metatype = true)
/**
 * @inheritDoc
 */
public class GrouperConfigurationImpl implements GrouperConfiguration {

	private static final Logger log = LoggerFactory
	.getLogger(GrouperConfigurationImpl.class);

	// Configurable via the ConfigAdmin services.
	private static final String DEFAULT_URL = "http://localhost:9090/grouper-ws/servicesRest";
	@Property(value = DEFAULT_URL)
	protected static final String PROP_URL = "grouper.url";

	private static final String DEFAULT_WS_VERSION = "1_7_000";
	@Property(value = DEFAULT_WS_VERSION)
	protected static final String PROP_WS_VERSION = "grouper.ws_version";

	private static final String DEFAULT_USERNAME = "GrouperSystem";
	@Property(value = DEFAULT_USERNAME)
	protected static final String PROP_USERNAME = "grouper.username";

	private static final String DEFAULT_PASSWORD = "abc123";
	@Property(value = DEFAULT_PASSWORD)
	protected static final String PROP_PASSWORD = "grouper.password";

	// HTTP Timeout in milliseconds
	private static final String DEFAULT_TIMEOUT = "5000";
	@Property(value = DEFAULT_TIMEOUT)
	protected static final String PROP_TIMEOUT = "grouper.httpTimeout";

	private static final String DEFAULT_IGNORED_USER = "grouper-admin";
	@Property(value = DEFAULT_IGNORED_USER)
	protected static final String PROP_IGNORED_USER = "grouper.ignoredUser";

	private static final String[] DEFAULT_IGNORED_GROUP_PATTERN = {"administrators"};
	@Property(value = { "administrators" }, cardinality = 1)
	protected static final String PROP_IGNORED_GROUP_PATTERN = "grouper.ignoredGroupsPatterns"; 

	// TODO: A better way to generate the default list.
	private static final String[] DEFAULT_PSEUDO_GROUP_SUFFIXES = 
		{"-manager", "-ta", "-lecturer", "-student", "-member"};
	@Property(value = {"-manager", "-ta", "-lecturer", "-student", "-member"}, cardinality = 1)
	protected static final String PROP_PSEUDO_GROUP_SUFFIXES = "grouper.specialGroupsSuffixes";

	private static final String DEFAULT_BASESTEM = "edu:apps:sakaioae";
	@Property(value = DEFAULT_BASESTEM)
	protected static final String PROP_BASESTEM = "grouper.basestem";

	private static final String[] DEFAULT_GROUPER_GROUP_TYPES = {"includeExcludeGroup"};
	@Property(value = {"includeExcludeGroup"}, cardinality = 1)
	protected static final String PROP_GROUPER_GROUP_TYPES = "grouper.groupTypes";

	// Grouper configuration.
	private URL url;
	private String username;
	private String password;
	private String baseStem;

	// GrouperWS
	private String wsVersion;
	private int httpTimeout;

	// Ignore events caused by this user
	private String ignoredUser;
	// Ignore groups that match these regexs
	private String[] ignoredGroupPatterns;

	// Suffixes that indicate these are sakai internal groups
	private String[] pseudoGroupSuffixes;

	// Grouper group types for newly created groups.
	private String[] groupTypes;


	// -------------------------- Configuration Admin --------------------------
	/**
	 * Copy in the configuration from the config admin service.
	 *
	 * Called by the Configuration Admin service when a new configuration is
	 * detected in the web console or a config file.
	 * 
	 * @see org.osgi.service.cm.ManagedService#updated
	 */
	@Activate
	@Modified
	public void updated(Map<?, ?> props) throws ConfigurationException {
		try {
			url = new URL(OsgiUtil.toString(props.get(PROP_URL), DEFAULT_URL));
		} catch (MalformedURLException mfe) {
			throw new ConfigurationException(PROP_URL, mfe.getMessage(), mfe);
		}
		username  = OsgiUtil.toString(props.get(PROP_USERNAME), DEFAULT_USERNAME);
		password  = OsgiUtil.toString(props.get(PROP_PASSWORD), DEFAULT_PASSWORD);
		baseStem = OsgiUtil.toString(props.get(PROP_BASESTEM),DEFAULT_BASESTEM);

		if (baseStem.endsWith(":")){
			baseStem = baseStem.substring(0, baseStem.length() - 1);
		}

		wsVersion = OsgiUtil.toString(props.get(PROP_WS_VERSION), DEFAULT_WS_VERSION);
		httpTimeout = OsgiUtil.toInteger(props.get(PROP_TIMEOUT), Integer.parseInt(DEFAULT_TIMEOUT));

		ignoredUser = OsgiUtil.toString(props.get(PROP_IGNORED_USER),DEFAULT_IGNORED_USER);
		ignoredGroupPatterns = getStringArrayProp(props.get(PROP_IGNORED_GROUP_PATTERN), DEFAULT_IGNORED_GROUP_PATTERN);
		pseudoGroupSuffixes = getStringArrayProp(props.get(PROP_PSEUDO_GROUP_SUFFIXES), DEFAULT_PSEUDO_GROUP_SUFFIXES);
		groupTypes = getStringArrayProp(props.get(PROP_GROUPER_GROUP_TYPES), DEFAULT_GROUPER_GROUP_TYPES);

		log.debug("Configured!");
	}

	public URL getUrl() {
		return url;
	}

	public String getWsVersion() {
		return wsVersion;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getRestWsUrlString() {
		return url + "/" + wsVersion;
	}

	public int getHttpTimeout() {
		return httpTimeout;
	}

	public String getIgnoredUserId() {
		return ignoredUser;
	}

	public String[] getIgnoredGroups() {
		return ignoredGroupPatterns;
	}

	public String[] getPseudoGroupSuffixes(){
		return pseudoGroupSuffixes;
	}

	public String getBaseStem(String groupType) {
		String stem = this.baseStem;
		if ("group".equals(groupType)){
			stem += ":groups";
		}
		else if ("contacts".equals(groupType)){
			stem += ":users";
		}
		return stem;
	}

	public String[] getGroupTypes(){
		return groupTypes;
	}

	/**
	 * @param value the value of the the property
	 * @param defaultValue the default value
	 * @return a String[] of values for that property or the defaultValue if null
	 */
	private static String[] getStringArrayProp(Object value, String[] defaultValue){
		String[] result = null;
		if (value == null){
			result = defaultValue;
		}
		else if (value instanceof String){
			result = new String[] { (String)value };
		}
		else {
			result = (String[])value;
		}
		return result;
	}
}