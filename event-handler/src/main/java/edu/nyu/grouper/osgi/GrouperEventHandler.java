package edu.nyu.grouper.osgi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.sakaiproject.nakamura.api.lite.util.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service(value = EventHandler.class)
@Component(immediate = true, metatype=true)
@Properties(value = { 
	@Property(name = EventConstants.EVENT_TOPIC, 
			value = {
			"org/sakaiproject/nakamura/lite/authorizables/ADDED",
			"org/sakaiproject/nakamura/lite/authorizables/DELETE"})
})
public class GrouperEventHandler implements EventHandler {

	private static final Logger log = LoggerFactory.getLogger(GrouperEventHandler.class);

	// Configurable via the ConfigAdmin services.
	private static final String DEFAULT_URL = "http://localhost:8080/grouper-ws";
	@Property(value=DEFAULT_URL)
	private static final String PROP_URL = "sakai.grouper.url";

	private static final String DEFAULT_USERNAME = "GrouperSystem";
	@Property(value=DEFAULT_USERNAME)
	private static final String PROP_USERNAME = "sakai.grouper.username";

	private static final String DEFAULT_PASSWORD = "abc123";
	@Property(value=DEFAULT_PASSWORD)
	private static final String PROP_PASSWORD = "sakai.grouper.password";

	private static final String DEFAULT_BASESTEM = "edu:apps:sakai3";
	@Property(value=DEFAULT_BASESTEM)
	private static final String PROP_BASESTEM = "sakai.grouper.basestem";

	private URL url;
	private String username;
	private String password;
	private String baseStem;

	public void handleEvent(Event event) {
		StringBuffer buffer = new StringBuffer();
		for(String name: Iterables.of(event.getPropertyNames())){
			buffer.append("\n" + name + "=" + event.getProperty(name));
		}
		log.info("topic : " + event.getTopic() + " properties: " + buffer.toString());

		if ("org/sakaiproject/nakamura/lite/authorizables/ADDED".equals(event.getTopic())){

		}
		if ("org/sakaiproject/nakamura/lite/authorizables/DELETE".equals(event.getTopic())){

		}
	}

	// -------------------------- Configuration Admin --------------------------

	/**
	 * Called by the Configuration Admin service when a new configuration is detected.
	 * @see org.osgi.service.cm.ManagedService#updated
	 */
	@Activate
	@Modified
	@SuppressWarnings("rawtypes")
	public void updated(Dictionary props) throws ConfigurationException {
		try {
			url = new URL(OsgiUtil.toString(props.get(PROP_URL), DEFAULT_URL));
			username = OsgiUtil.toString(props.get(PROP_USERNAME), DEFAULT_USERNAME);
			password = OsgiUtil.toString(props.get(PROP_PASSWORD), DEFAULT_PASSWORD);
			baseStem = OsgiUtil.toString(props.get(PROP_BASESTEM), DEFAULT_BASESTEM);
		}
		catch (MalformedURLException mfe) {
			throw new ConfigurationException(PROP_URL, mfe.getMessage(), mfe);
		}
	}
}
