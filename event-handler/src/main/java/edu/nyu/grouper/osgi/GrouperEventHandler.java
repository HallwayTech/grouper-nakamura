package edu.nyu.grouper.osgi;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import edu.nyu.grouper.api.GrouperManager;

@Service
@Component(immediate = true, metatype=true)
@Properties(value = { 
		@Property(name = EventConstants.EVENT_TOPIC, 
				value = {
				"org/sakaiproject/nakamura/lite/authorizables/ADDED",
				"org/sakaiproject/nakamura/lite/authorizables/UPDATED"
		})
})
public class GrouperEventHandler implements EventHandler {
	
	@Reference
	protected GrouperManager grouperManager;

	/**
	 * Respond to Group events in SakaiOAE by issuing WS calls to Grouper. 
	 */
	public void handleEvent(Event event) {

		if ("org/sakaiproject/nakamura/lite/authorizables/ADDED".equals(event.getTopic())){
			String groupId = (String) event.getProperty("path");
			grouperManager.createGroup(groupId);
		}
		
		if ("org/sakaiproject/nakamura/lite/authorizables/UPDATED".equals(event.getTopic())){
			String groupId = (String) event.getProperty("path");
			grouperManager.updateGroup(groupId, event);
		}
	}
}