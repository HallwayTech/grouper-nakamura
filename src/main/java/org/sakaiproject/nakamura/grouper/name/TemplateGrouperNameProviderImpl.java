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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
import org.sakaiproject.nakamura.grouper.name.api.GrouperNameProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Create grouper name information from the authorizableId of a {@link Group}.
 * 
 * To use this component you have to define two things:
 * 1. A regular expression to apply to the authorizableId (grouper.groupId.regex)
 * 2. An output template to create a string for the grouper name. (grouper.name.template)
 * 
 */
@Component(metatype = true)
@Service
@Properties(value = {
		@Property(name = "service.ranking", intValue = 100)
})
public class TemplateGrouperNameProviderImpl implements GrouperNameProvider {

	private static final Logger log = LoggerFactory.getLogger(TemplateGrouperNameProviderImpl.class);

	@Reference
	GrouperConfiguration config;

	private static final String DEFAULT_GROUPID_PATTERN = "";
	@Property(value = DEFAULT_GROUPID_PATTERN)
	protected static final String PROP_GROUPID_PATTERN = "grouper.nameprovider.template.groupIdPattern";

	private static final String DEFAULT_GROUPERNAME_TEMPLATE = "";
	@Property(value = DEFAULT_GROUPERNAME_TEMPLATE)
	protected static final String PROP_GROUPERNAME_TEMPLATE = "grouper.nameprovider.template.grouperName";

	// Pattern used to parse sakaiOAE group id's
	private String groupIdPatternString;
	private Pattern groupIdPattern;

	// Template used to render the grouper name
	private String grouperNameTemplate;

	private Template template;
	private boolean templateReady = false;

	@Activate
	public void activate(Map<?,?> props) {
		modified(props);
	}

	@Modified
	public void modified(Map<?,?> props) {
		grouperNameTemplate = OsgiUtil.toString(props.get(PROP_GROUPERNAME_TEMPLATE),DEFAULT_GROUPERNAME_TEMPLATE);
		groupIdPatternString = OsgiUtil.toString(props.get(PROP_GROUPID_PATTERN),DEFAULT_GROUPID_PATTERN);
		groupIdPattern = Pattern.compile(groupIdPatternString);

		try {
			if (grouperNameTemplate != null && grouperNameTemplate.length() > 5){
				Velocity.init();
				RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
				SimpleNode node = runtimeServices.parse(new StringReader(grouperNameTemplate), "Grouper name template");
				template = new Template();
				template.setRuntimeServices(runtimeServices);
				template.setData(node);
				template.initDocument();
				templateReady = true;
			}
		}
		catch (ParseException pe) {
			log.error("Could not parse the velocity template, {}", pe.getMessage());
		}
	}

	@Override
	public String getGrouperName(String groupId) {
		if (groupId == null || templateReady == false){
			return null;
		}
		Matcher m = groupIdPattern.matcher(groupId);
		if (! m.find() || m.groupCount() == 0){
			throw new RuntimeException("Did not match " + groupId);
		}
		ArrayList<String> g = new ArrayList<String>();
		for(int i = 0; i <= m.groupCount(); i++){
			g.add(m.group(i));
		}
		VelocityContext context = new VelocityContext();
		context.put("g", g);
		context.put("extension", BaseGrouperNameProvider.getGrouperExtension(groupId, config));

		StringWriter sw = new StringWriter();
		template.merge(context, sw);

		// If this is a pseudogroup lop off the -rolename and make it the grouper group name.
		String grouperName = sw.toString();
		String[] split = StringUtils.split(grouperName, ':');
		for (String suffix: config.getPseudoGroupSuffixes()){
			if (split[split.length - 2].endsWith(suffix)){
				split[split.length - 2] = split[split.length - 2].substring(0, split[split.length - 2].indexOf(suffix)); 
			}
		}
		grouperName = StringUtils.join(split, ':');
		log.debug("{} => {}", groupId, grouperName);
		return grouperName;
	}

	public void bindGrouperConfiguration(GrouperConfiguration gconfig) {
		config = gconfig;
	}

}
