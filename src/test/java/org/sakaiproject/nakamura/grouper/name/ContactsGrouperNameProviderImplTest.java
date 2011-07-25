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

import static junit.framework.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.nakamura.grouper.GrouperConfigurationImpl;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class ContactsGrouperNameProviderImplTest {

	private ContactsGrouperNameProviderImpl provider;
	
	@Test
	public void testGetGrouperName() throws Exception {
		
		Map<String,String> m = new HashMap<String,String>();
		GrouperConfigurationImpl gconfig = new GrouperConfigurationImpl();
		gconfig.updated(m);
			
		provider = new ContactsGrouperNameProviderImpl();
		provider.bindGrouperConfiguration((GrouperConfiguration)gconfig);
		
		assertEquals(null, provider.getGrouperName(null));
		assertEquals(null, provider.getGrouperName("bleep"));
		assertEquals("user1:contacts", provider.getGrouperName("g-contacts-user1"));
	}
}