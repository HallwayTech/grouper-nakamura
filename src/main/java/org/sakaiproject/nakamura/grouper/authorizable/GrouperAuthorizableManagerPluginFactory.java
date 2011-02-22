package org.sakaiproject.nakamura.grouper.authorizable;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManagerPlugin;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManagerPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service(value = AuthorizableManagerPluginFactory.class)
public class GrouperAuthorizableManagerPluginFactory implements AuthorizableManagerPluginFactory {

	private static final Logger log = LoggerFactory.getLogger(GrouperAuthorizableManagerPlugin.class);

	private Collection<AuthorizableManagerPlugin> instances;
	
	public GrouperAuthorizableManagerPluginFactory(){
		instances = new ArrayList<AuthorizableManagerPlugin>();
	}
	
	@Activate
	public void activate(){
		log.debug("GrouperAuthorizableManagerPluginFactory Activated!");
	}
	
	public AuthorizableManagerPlugin getAuthorizableManagerPlugin() {
		GrouperAuthorizableManagerPlugin gamp = new GrouperAuthorizableManagerPlugin(this);
		instances.add(gamp);
		return gamp;
	}
	
	public void removePlugin(GrouperAuthorizableManagerPlugin gamp){
		this.instances.remove(gamp);
	}

}
