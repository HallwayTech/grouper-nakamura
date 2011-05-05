package edu.nyu.grouper.util;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

public class GrouperJsonUtil {
	
	/**
	   * Convert an object to json. Stolen from GrouperUtil.jsonConvertTo
	   * @param object
	   * @return the string of json
	   */
	  public static String toJSONString(Object object) {
	    if (object == null) {
	      throw new NullPointerException();
	    }

	    JsonConfig jsonConfig = new JsonConfig();  
	    jsonConfig.setJsonPropertyFilter( new PropertyFilter(){  
	       public boolean apply( Object source, String name, Object value ) {  
	          return value == null; 
	       }  
	    });  
	    JSONObject jsonObject = JSONObject.fromObject( object, jsonConfig );  
	    String json = jsonObject.toString();
	    
	    return "{\"" + object.getClass().getSimpleName() + "\":" + json + "}";
	  }

}
