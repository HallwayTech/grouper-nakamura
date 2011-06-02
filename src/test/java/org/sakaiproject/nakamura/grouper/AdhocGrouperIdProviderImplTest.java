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

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.isA;

import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class AdhocGrouperIdProviderImplTest {

	private AdhocGrouperIdProviderImpl provider;
	
	@Mock
	private Repository repository;
	
	@Mock
	private Session session;
	
	@Mock
	private AuthorizableManager authorizableManager;

	@Mock
	private Authorizable authorizable;
	
	@Test
	public void testGetGrouperName() throws Exception {
		
		Map<String,String> m = new HashMap<String,String>();
		m.put(GrouperConfigurationImpl.PROP_BASESTEM, "some:base:stem");
		GrouperConfigurationImpl gconfig = new GrouperConfigurationImpl();
		gconfig.updated(m);
		
		when(repository.loginAdministrative()).thenReturn(session);
		when(session.getAuthorizableManager()).thenReturn(authorizableManager);
		when(authorizableManager.findAuthorizable(isA(String.class))).thenReturn(authorizable);
		when(authorizable.getProperty(isA(String.class))).thenReturn(null);
		
		provider = new AdhocGrouperIdProviderImpl();
		provider.bindGrouperConfiguration((GrouperConfiguration)gconfig);
		provider.repository = repository;
		
		assertEquals(null, provider.getGrouperName(null));
		assertEquals("some:base:stem:adhoc:s:so:some:members", provider.getGrouperName("some"));
		assertEquals("some:base:stem:adhoc:s:so:some_group:managers", provider.getGrouperName("some_group-managers"));
		assertEquals("some:base:stem:adhoc:s:so:some_group:ta", provider.getGrouperName("some_group-ta"));
	}
}