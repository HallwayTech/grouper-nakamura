package org.sakaiproject.nakamura.grouper;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;

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
		assertEquals("members", BaseGrouperNameProvider.getGrouperExtension("group1", config));
		assertEquals("members", BaseGrouperNameProvider.getGrouperExtension("some-thing", config));
		
		assertEquals("managers", BaseGrouperNameProvider.getGrouperExtension("group1-managers", config));
		assertEquals("ta", BaseGrouperNameProvider.getGrouperExtension("group1-ta", config));		
	}
	
	@Test
	public void testGetGrouperLastStem(){
		assertNull(BaseGrouperNameProvider.getGrouperLastStem(null, config));
		assertEquals("group1", BaseGrouperNameProvider.getGrouperLastStem("group1", config));
		assertEquals("group1", BaseGrouperNameProvider.getGrouperLastStem("group1-managers", config));
		assertEquals("group1", BaseGrouperNameProvider.getGrouperLastStem("group1-ta", config));

		assertEquals("some-thing", BaseGrouperNameProvider.getGrouperLastStem("some-thing", config));
		assertEquals("some-thing", BaseGrouperNameProvider.getGrouperLastStem("some-thing-ta", config));
		assertEquals("some-thing", BaseGrouperNameProvider.getGrouperLastStem("some-thing-managers", config));
		assertEquals("some-thing-98y8og", BaseGrouperNameProvider.getGrouperLastStem("some-thing-98y8og", config));
	}
}
