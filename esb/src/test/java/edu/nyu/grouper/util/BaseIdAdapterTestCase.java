package edu.nyu.grouper.util;

import edu.nyu.grouper.util.BaseNakamuraGroupIdAdapter;
import junit.framework.TestCase;

public class BaseIdAdapterTestCase extends TestCase {
	
	BaseNakamuraGroupIdAdapter adapter;
	
	@Override
	public void setUp(){
		adapter = new BaseNakamuraGroupIdAdapter("edu:apps:stem1");
	}
	
	public void testGetNakamuraName(){
		assertEquals(null, adapter.getNakamuraGroupId(null));
		assertEquals("name1", adapter.getNakamuraGroupId("edu:apps:stem1:name1"));
		assertEquals("name1_name2", adapter.getNakamuraGroupId("edu:apps:stem1:name1:name2"));
	}
	
	public void testStripStem(){
		assertEquals(null, adapter.stripBaseStem(null));
		assertEquals("name1", adapter.stripBaseStem("edu:apps:stem1:name1"));
		assertEquals("name1:name2", adapter.stripBaseStem("edu:apps:stem1:name1:name2"));
		assertEquals("name1", adapter.stripBaseStem("name1"));
	}
}