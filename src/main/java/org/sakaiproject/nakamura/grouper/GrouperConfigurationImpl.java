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
import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;

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

	private static final String DEFAULT_GROUPID_PATTERN = "";
	@Property(value = DEFAULT_GROUPID_PATTERN)
	protected static final String PROP_GROUPID_PATTERN = "grouper.groupIdPattern"; 

	private static final String DEFAULT_GROUPERNAME_TEMPLATE = "";
	@Property(value = DEFAULT_GROUPERNAME_TEMPLATE)
	protected static final String PROP_GROUPERNAME_TEMPLATE = "grouper.name.template"; 

	// TODO: A better way to generate the default list.
	private static final String[] DEFAULT_PSEUDO_GROUP_SUFFIXES = 
		{"-manager", "-ta", "-lecturer", "-student", "-member"};
	@Property(value = {"-manager", "-ta", "-lecturer", "-student", "-member"}, cardinality = 1)
	protected static final String PROP_PSEUDO_GROUP_SUFFIXES = "grouper.specialGroupsSuffixes";

	private static final String DEFAULT_BASESTEM = "edu:apps:sakaioae";
	@Property(value = DEFAULT_BASESTEM)
	protected static final String PROP_BASESTEM = "grouper.basestem";

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

	// Pattern used to parse sakaiOAE group id's
	private String groupIdPatternString;
	private Pattern groupIdPattern;

	// Template used to render the grouper name
	private String grouperNameTemplate;

	// Suffixes that indicate these are sakai internal groups
	private String[] pseudoGroupSuffixes;


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

		wsVersion = OsgiUtil.toString(props.get(PROP_WS_VERSION), DEFAULT_WS_VERSION);
		httpTimeout = OsgiUtil.toInteger(props.get(PROP_TIMEOUT), Integer.parseInt(DEFAULT_TIMEOUT));

		ignoredUser = OsgiUtil.toString(props.get(PROP_IGNORED_USER),DEFAULT_IGNORED_USER);
		groupIdPatternString = OsgiUtil.toString(props.get(PROP_GROUPID_PATTERN),DEFAULT_GROUPID_PATTERN);
		grouperNameTemplate = OsgiUtil.toString(props.get(PROP_GROUPERNAME_TEMPLATE),DEFAULT_GROUPERNAME_TEMPLATE);

		Object ig = OsgiUtil.toStringArray(props.get(PROP_IGNORED_GROUP_PATTERN), DEFAULT_IGNORED_GROUP_PATTERN);
		if (ig == null){
			ignoredGroupPatterns = DEFAULT_IGNORED_GROUP_PATTERN;
		}
		else if (ig instanceof String){
			ignoredGroupPatterns = new String[] { (String)ig };
		}
		else {
			ignoredGroupPatterns = (String[])ig;
		}

		Object pgs = OsgiUtil.toStringArray(props.get(PROP_PSEUDO_GROUP_SUFFIXES), DEFAULT_PSEUDO_GROUP_SUFFIXES);
		if (pgs == null){
			pseudoGroupSuffixes = DEFAULT_PSEUDO_GROUP_SUFFIXES;
		}
		else if (pgs instanceof String){
			pseudoGroupSuffixes = new String[] { (String)pgs };
		}
		else {
			pseudoGroupSuffixes = (String[])pgs;
		}

		groupIdPattern = Pattern.compile(groupIdPatternString);
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

	public Pattern getGroupIdPattern() {
		return groupIdPattern;
	}

	public String getGrouperNameTemplate() {
		return grouperNameTemplate;
	}

	public String[] getPseudoGroupSuffixes(){
		return pseudoGroupSuffixes;
	}

	@Override
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
}