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
package edu.nyu.grouper;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.nyu.grouper.AggregateGroupsGrouperIdHelperImpl;
import edu.nyu.grouper.GrouperConfigurationImpl;
import edu.nyu.grouper.api.GrouperConfiguration;

public class AggregateGroupsGrouperIdHelperImplTest extends TestCase {

	private AggregateGroupsGrouperIdHelperImpl idHelper;
	private static final String BASE_STEM = "some:base:stem";
	
	@Before
	public void setUp() throws Exception {
		// Test grouper configs
		Map<String,String> m = new HashMap<String,String>();
		m.put(GrouperConfigurationImpl.PROP_BASESTEM, BASE_STEM);
		GrouperConfigurationImpl gconfig = new GrouperConfigurationImpl();
		gconfig.updated(m);
		
		idHelper = new AggregateGroupsGrouperIdHelperImpl();
		idHelper.bindGrouperConfiguration((GrouperConfiguration)gconfig);
	}
	
	@Test
	public void testGetGrouperName(){
		assertEquals(null, idHelper.getGrouperName(null));
		assertEquals(BASE_STEM + ":group1:members_sakaioae",
					idHelper.getGrouperName("group1"));
		assertEquals(BASE_STEM + ":group1:managers_sakaioae",
					idHelper.getGrouperName("group1-managers"));
		
		assertEquals(BASE_STEM + ":gstem1:gstem2:gext:members_sakaioae",
					idHelper.getGrouperName("gstem1_gstem2_gext"));
		assertEquals(BASE_STEM + ":gstem1:gstem2:gext:managers_sakaioae",
					idHelper.getGrouperName("gstem1_gstem2_gext-managers"));
		assertEquals(BASE_STEM + ":gstem1:gstem2:gext:ta_sakaioae",
				idHelper.getGrouperName("gstem1_gstem2_gext-ta"));
	}
	
	@Test
	public void testGetGrouperExtension(){
		assertNull(idHelper.getGrouperExtension(null));
		assertEquals("members_sakaioae",
				idHelper.getGrouperExtension("group1"));
		assertEquals("managers_sakaioae",
				idHelper.getGrouperExtension("group1-managers"));
		assertEquals("ta_sakaioae",
				idHelper.getGrouperExtension("group1-ta"));
	}
	
	@Test
	public void testGetFullStem(){
		assertEquals(null, idHelper.getFullStem(null));
		
		assertEquals(BASE_STEM + ":group1", idHelper.getFullStem("group1"));
		assertEquals(BASE_STEM + ":group1", idHelper.getFullStem("group1-managers"));
		
		assertEquals(BASE_STEM + ":stem1:group1", idHelper.getFullStem("stem1_group1"));
		assertEquals(BASE_STEM + ":stem1:group1", idHelper.getFullStem("stem1_group1-managers"));
		assertEquals(BASE_STEM + ":stem1:group1", idHelper.getFullStem("stem1_group1-ta"));
	}
	
}