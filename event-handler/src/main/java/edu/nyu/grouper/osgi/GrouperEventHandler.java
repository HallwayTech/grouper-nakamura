package edu.nyu.grouper.osgi;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.EventConstants;
import org.sakaiproject.nakamura.api.lite.util.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component(immediate = true)
@Properties(value = { 
	@Property(name = EventConstants.EVENT_TOPIC, 
			value = {
			"org/sakaiproject/nakamura/lite/authorizables/ADDED",
			"org/sakaiproject/nakamura/lite/authorizables/DELETE"})
})
public class GrouperEventHandler implements EventHandler {

	private static final Logger log = LoggerFactory.getLogger(GrouperEventHandler.class);
	
	public void handleEvent(Event event) {
		StringBuffer buffer = new StringBuffer();
		for(String name: Iterables.of(event.getPropertyNames())){
			buffer.append("\n" + name + "=" + event.getProperty(name));
		}
		log.info("topic : " + event.getTopic() + " properties: " + buffer.toString());
	}

}
