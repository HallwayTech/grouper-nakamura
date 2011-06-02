package org.sakaiproject.nakamura.grouper;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;

public class BaseGrouperIdProviderImplTest extends TestCase {
	
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
		assertNull(BaseGrouperIdProvider.getGrouperExtension(null, config));
		assertEquals("members", BaseGrouperIdProvider.getGrouperExtension("group1", config));
		assertEquals("members", BaseGrouperIdProvider.getGrouperExtension("some-thing", config));
		
		assertEquals("managers", BaseGrouperIdProvider.getGrouperExtension("group1-managers", config));
		assertEquals("ta", BaseGrouperIdProvider.getGrouperExtension("group1-ta", config));		
	}
	
	@Test
	public void testGetGrouperLastStem(){
		assertNull(BaseGrouperIdProvider.getGrouperLastStem(null, config));
		assertEquals("group1", BaseGrouperIdProvider.getGrouperLastStem("group1", config));
		assertEquals("group1", BaseGrouperIdProvider.getGrouperLastStem("group1-managers", config));
		assertEquals("group1", BaseGrouperIdProvider.getGrouperLastStem("group1-ta", config));

		assertEquals("some-thing", BaseGrouperIdProvider.getGrouperLastStem("some-thing", config));
		assertEquals("some-thing", BaseGrouperIdProvider.getGrouperLastStem("some-thing-ta", config));
		assertEquals("some-thing", BaseGrouperIdProvider.getGrouperLastStem("some-thing-managers", config));
		assertEquals("some-thing-98y8og", BaseGrouperIdProvider.getGrouperLastStem("some-thing-98y8og", config));
	}
}
