package org.sakaiproject.nakamura.grouper;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;

public class BaseGrouperIdManagerTest extends TestCase {
	
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
		assertNull(BaseGrouperIdManager.getGrouperExtension(null, config));
		assertEquals("members", BaseGrouperIdManager.getGrouperExtension("group1", config));
		assertEquals("members", BaseGrouperIdManager.getGrouperExtension("some-thing", config));
		
		assertEquals("managers", BaseGrouperIdManager.getGrouperExtension("group1-managers", config));
		assertEquals("ta", BaseGrouperIdManager.getGrouperExtension("group1-ta", config));		
	}
}
