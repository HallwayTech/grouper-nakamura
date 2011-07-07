package org.sakaiproject.nakamura.grouper.name;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;
import org.sakaiproject.nakamura.grouper.GrouperConfigurationImpl;

public class BaseGrouperNameProviderImplTest extends TestCase {
	
	private GrouperConfigurationImpl config;
	
	@Override
	public void setUp(){
		config = new GrouperConfigurationImpl();
		try {
			config.updated(new HashMap<String,Object>());
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetGrouperExtension(){
		assertNull(BaseGrouperNameProvider.getGrouperExtension(null, config));
		assertEquals("member", BaseGrouperNameProvider.getGrouperExtension("group1", config));
		assertEquals("member", BaseGrouperNameProvider.getGrouperExtension("some-thing", config));
		assertEquals("member", BaseGrouperNameProvider.getGrouperExtension("some-thing-member", config));
		assertEquals("member", BaseGrouperNameProvider.getGrouperExtension("some-thing_member", config));

		assertEquals("manager", BaseGrouperNameProvider.getGrouperExtension("group1-manager", config));
		assertEquals("manager", BaseGrouperNameProvider.getGrouperExtension("group1-meh-manager", config));
		assertEquals("manager", BaseGrouperNameProvider.getGrouperExtension("group1_meh-manager", config));
		assertEquals("ta", BaseGrouperNameProvider.getGrouperExtension("group1-ta", config));
		assertEquals("student", BaseGrouperNameProvider.getGrouperExtension("group1-student", config));

		assertEquals("contacts", BaseGrouperNameProvider.getGrouperExtension("g-contacts-user1", config));
	}
	
	@Test
	public void testGetGrouperLastStem(){
		assertNull(BaseGrouperNameProvider.getGrouperLastStem(null, config));
		assertEquals("group1", BaseGrouperNameProvider.getGrouperLastStem("group1", config));
		assertEquals("group1", BaseGrouperNameProvider.getGrouperLastStem("group1-member", config));
		assertEquals("group1", BaseGrouperNameProvider.getGrouperLastStem("group1-ta", config));
		assertEquals("group1", BaseGrouperNameProvider.getGrouperLastStem("group1-manager", config));

		assertEquals("group1_member", BaseGrouperNameProvider.getGrouperLastStem("group1_member", config));
		assertEquals("group1_manager", BaseGrouperNameProvider.getGrouperLastStem("group1_manager", config));
		assertEquals("group1_ta", BaseGrouperNameProvider.getGrouperLastStem("group1_ta", config));

		assertEquals("some-thing", BaseGrouperNameProvider.getGrouperLastStem("some-thing", config));
		assertEquals("some-thing", BaseGrouperNameProvider.getGrouperLastStem("some-thing-ta", config));
		assertEquals("some-thing", BaseGrouperNameProvider.getGrouperLastStem("some-thing-manager", config));
		assertEquals("some-thing-98y8og", BaseGrouperNameProvider.getGrouperLastStem("some-thing-98y8og", config));

		assertEquals("user1", BaseGrouperNameProvider.getGrouperLastStem("g-contacts-user1", config));
	}
}
