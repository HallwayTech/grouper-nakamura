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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;
import org.sakaiproject.nakamura.grouper.GrouperConfigurationImpl;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;

public class TemplateGrouperNameProviderImplTest extends TestCase {
	
	@Test
	public void testGetGrouperName() throws ConfigurationException{

		GrouperConfigurationImpl gconfig = new GrouperConfigurationImpl();
		Map<String,String> m = new HashMap<String,String>();
		m.put(GrouperConfigurationImpl.PROP_EXTENSION_OVERRIDES, "meh:bleh");
		gconfig.updated(m);
		
		TemplateGrouperNameProviderImpl provider = new TemplateGrouperNameProviderImpl();
		m.put(TemplateGrouperNameProviderImpl.PROP_GROUPID_PATTERN, "([^:]+)_([^:]+)");
		m.put(TemplateGrouperNameProviderImpl.PROP_GROUPERNAME_TEMPLATE, "some:base:stem:$g[1]:$g[2]:$extension");
		provider.modified(m);
		provider.bindGrouperConfiguration((GrouperConfiguration)gconfig);
		
		assertEquals(null, provider.getGrouperName(null));
		assertEquals("some:base:stem:some:group:member", provider.getGrouperName("some_group"));
		assertEquals("some:base:stem:some:group:manager", provider.getGrouperName("some_group-manager"));
		assertEquals("some:base:stem:some:group:ta", provider.getGrouperName("some_group-ta"));
	}
}