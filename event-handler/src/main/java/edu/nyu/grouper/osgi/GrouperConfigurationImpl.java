package edu.nyu.grouper.osgi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nyu.grouper.osgi.api.GrouperConfiguration;

@Service
@Component(immediate = true, metatype=true)
public class GrouperConfigurationImpl implements GrouperConfiguration {

	private static final Logger log = LoggerFactory.getLogger(GrouperConfigurationImpl.class);
	
	// Configurable via the ConfigAdmin services.
	private static final String DEFAULT_URL = "http://localhost:9090/grouper-ws/servicesRest";
	@Property(value=DEFAULT_URL)
	private static final String PROP_URL = "sakai.grouper.url";
	
	private static final String DEFAULT_WS_VERSION = "1_6_003";
	@Property(value=DEFAULT_WS_VERSION)
	private static final String PROP_WS_VERSION= "sakai.grouper.ws_version";

	private static final String DEFAULT_USERNAME = "GrouperSystem";
	@Property(value=DEFAULT_USERNAME)
	private static final String PROP_USERNAME = "sakai.grouper.username";

	private static final String DEFAULT_PASSWORD = "abc123";
	@Property(value=DEFAULT_PASSWORD)
	private static final String PROP_PASSWORD = "sakai.grouper.password";

	private static final String DEFAULT_BASESTEM = "edu:apps:sakai3";
	@Property(value=DEFAULT_BASESTEM)
	private static final String PROP_BASESTEM = "sakai.grouper.basestem";
	
	// Grouper configuration.
	private URL url;
	private String wsVersion;
	private String username;
	private String password;
	private String baseStem;

	// -------------------------- Configuration Admin --------------------------
	/**
	 * Called by the Configuration Admin service when a new configuration is detected.
	 * @see org.osgi.service.cm.ManagedService#updated
	 */
	@Activate
	@Modified
	public void updated(Map<?,?> props) throws ConfigurationException {
		try {
			url = new URL(OsgiUtil.toString(props.get(PROP_URL), DEFAULT_URL));
			wsVersion = OsgiUtil.toString(props.get(PROP_WS_VERSION), DEFAULT_WS_VERSION);
			username = OsgiUtil.toString(props.get(PROP_USERNAME), DEFAULT_USERNAME);
			password = OsgiUtil.toString(props.get(PROP_PASSWORD), DEFAULT_PASSWORD);
			baseStem = OsgiUtil.toString(props.get(PROP_BASESTEM), DEFAULT_BASESTEM);
		}
		catch (MalformedURLException mfe) {
			throw new ConfigurationException(PROP_URL, mfe.getMessage(), mfe);
		}
		log.debug("Configured!");
	}

	public URL getUrl() {
		return url;
	}

	/* (non-Javadoc)
	 * @see edu.nyu.grouper.osgi.GrouperConfiguration#getWsVersion()
	 */
	@Override
	public String getWsVersion() {
		return wsVersion;
	}

	/* (non-Javadoc)
	 * @see edu.nyu.grouper.osgi.GrouperConfiguration#getUsername()
	 */
	@Override
	public String getUsername() {
		return username;
	}

	/* (non-Javadoc)
	 * @see edu.nyu.grouper.osgi.GrouperConfiguration#getPassword()
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see edu.nyu.grouper.osgi.GrouperConfiguration#getBaseStem()
	 */
	@Override
	public String getBaseStem() {
		return baseStem;
	}

	/* (non-Javadoc)
	 * @see edu.nyu.grouper.osgi.GrouperConfiguration#getRestWsGroupUrlString()
	 */
	@Override
	public String getRestWsGroupUrlString() {
		return url + "/" + wsVersion + "/groups";
	}	
}