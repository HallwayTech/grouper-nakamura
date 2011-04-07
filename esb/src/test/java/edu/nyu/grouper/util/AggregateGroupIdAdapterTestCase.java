package edu.nyu.grouper.util;

import junit.framework.TestCase;
import edu.nyu.grouper.util.AggregateGroupIdAdapter;

public class AggregateGroupIdAdapterTestCase extends TestCase {
	
	AggregateGroupIdAdapter adapter;
	
	@Override
	public void setUp(){
		adapter = new AggregateGroupIdAdapter("edu:apps:stem1");
	}
	
	public void testGetNakamuraGroupId(){
		assertEquals("name1", adapter.getNakamuraGroupId("edu:apps:stem1:name1:members"));
		assertEquals("name1-managers", adapter.getNakamuraGroupId("edu:apps:stem1:name1:managers"));
		
		assertEquals("name1_name2", adapter.getNakamuraGroupId("edu:apps:stem1:name1:name2:members"));
		assertEquals("name1_name2-managers", adapter.getNakamuraGroupId("edu:apps:stem1:name1:name2:managers"));
	}
}