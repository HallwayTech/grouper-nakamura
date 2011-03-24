package edu.nyu.grouper.test;

import edu.nyu.grouper.util.BaseNakamuraGroupIdAdapter;
import junit.framework.TestCase;

public class BaseIdAdapterTestCase extends TestCase {
	
	BaseNakamuraGroupIdAdapter adapter;
	
	@Override
	public void setUp(){
		adapter = new BaseNakamuraGroupIdAdapter("edu:apps:stem1");
	}
	
	public void testGetNakamuraName(){
		assertEquals("name1", adapter.getNakamuraName("edu:apps:stem1:name1"));
		assertEquals("name1_name2", adapter.getNakamuraName("edu:apps:stem1:name1:name2"));
	}
		
	public void testGetGrouperFullName() {
		assertEquals("edu:apps:stem1:name1", adapter.getGrouperFullName("name1"));
		assertEquals("edu:apps:stem1:name1:name2", adapter.getGrouperFullName("name1_name2"));
	}
}