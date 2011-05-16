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
package org.sakaiproject.nakamura.grouper.util;

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
