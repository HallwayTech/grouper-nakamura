package org.sakaiproject.nakamura.grouper.authorizable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManagerPlugin;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManagerPluginFactory;
import org.sakaiproject.nakamura.grouper.api.GrouperWSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(metatype = true, configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@Service
public class GrouperAuthorizableManagerPluginFactory 
		implements AuthorizableManagerPluginFactory, ManagedService {

	private static final Logger log = LoggerFactory.getLogger(GrouperAuthorizableManagerPlugin.class);

	// Configurable via the ConfigAdmin services.
	private static final String DEFAULT_URL = "";
	@Property(value=DEFAULT_URL)
	private static final String PROP_URL = "sakai.grouper.url";
	
	private static final String DEFAULT_USERNAME = "";
	@Property(value=DEFAULT_USERNAME)
	private static final String PROP_USERNAME = "sakai.grouper.username";
	
	private static final String DEFAULT_PASSWORD = "";
	@Property(value=DEFAULT_PASSWORD)
	private static final String PROP_PASSWORD = "sakai.grouper.password";

	// Most recently created configuration.
	private GrouperWSConfiguration grouperConfig;	

	// Keep a reference to all of the instances we create so we can talk to them later.
	private Collection<GrouperAuthorizableManagerPlugin> instances;

	public GrouperAuthorizableManagerPluginFactory(){
		instances = new ArrayList<GrouperAuthorizableManagerPlugin>();
	}

	@Activate
	public void activate(){
		log.debug("Activated!");
	}

	/*
	 * Main service method. 
	 */
	public AuthorizableManagerPlugin getAuthorizableManagerPlugin() {
		GrouperAuthorizableManagerPlugin gamp = null;
		if (getGrouperConfiguration() != null ) {
			gamp = new GrouperAuthorizableManagerPlugin(this, grouperConfig);
			instances.add(gamp);
		}
		return gamp;
	}

	public void removePlugin(GrouperAuthorizableManagerPlugin gamp){
		this.instances.remove(gamp);
	}

	// -------------------------- Configuration --------------------------
	
	/**
	 * Called by the Coniguration Admin service when a new configuration is 
	 * detected by Fileinstall
	 */
	@SuppressWarnings("rawtypes")
	public void updated(Dictionary props) throws ConfigurationException {
		GrouperWSConfiguration configuration = 
				GrouperAuthorizableManagerPluginFactory.createConfig(props);
		setGrouperConfiguration(configuration);

		if (configuration != null){
			log.debug("Configured : {}", (String)props.get(PROP_URL));
			for (GrouperAuthorizableManagerPlugin gamp : instances) {
				gamp.setGrouperConfig(getGrouperConfiguration());
			}
		}
	}
	
	/**
	 * Parse the configuration provided by the OSGI container into a usable
	 * configuration object. 
	 * @param props the configuration values from the Config Admin.
	 * @return the parsed configuration or empty config if there's a problem parsing.
	 */
	public static GrouperWSConfiguration createConfig(Dictionary<?,?> props){
		if (props == null ) {
			log.debug("Empty configuration.");
			return null;
		}
		GrouperWSConfiguration grouperConfig = new GrouperWSConfiguration();
		try {
			grouperConfig.setUrl(new URL((String)props.get(PROP_URL)));
			grouperConfig.setUsername(OsgiUtil.toString(props.get(PROP_USERNAME), DEFAULT_USERNAME));
			grouperConfig.setPassword(OsgiUtil.toString(props.get(PROP_PASSWORD), DEFAULT_PASSWORD));
		}
		catch (MalformedURLException mue){
			log.error("Malformed Grouper WS URL in config : {}", (String)props.get(PROP_URL));
		}
		return grouperConfig;
	}
	
	public GrouperWSConfiguration getGrouperConfiguration(){
		return this.grouperConfig;
	}

	public void setGrouperConfiguration(GrouperWSConfiguration configuration){
		this.grouperConfig = configuration;
	}

}
