package edu.nyu.grouper.osgi;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.sakaiproject.nakamura.api.lite.StoreListener;

import edu.nyu.grouper.api.GrouperManager;
import edu.nyu.grouper.osgi.api.GrouperConfiguration;

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
	protected GrouperConfiguration grouperConfiguration;

	@Reference
	protected GrouperManager grouperManager;

	/**
	 * Respond to Group events in SakaiOAE using a {@link GrouperManager}
	 * 
	 * {@inheritDoc}
	 */
	public void handleEvent(Event event) {

		if (ignoreEvent(event)){
			return;
		}

		if ("org/sakaiproject/nakamura/lite/authorizables/ADDED".equals(event.getTopic())){
			String groupId = (String) event.getProperty("path");
			grouperManager.createGroup(groupId);
		}
		
		if ("org/sakaiproject/nakamura/lite/authorizables/UPDATED".equals(event.getTopic())){
			String groupId = (String) event.getProperty("path");
			grouperManager.updateGroup(groupId, event);
		}
	}

	private boolean ignoreEvent(Event event){
		boolean ignore = false;
		/*
		 * Ignore events that were posted by the Grouper system to sakai so we don't wind up
		 * with a feedback loop between sakai and Grouper.
		 */
		if (grouperConfiguration.getIgnoredUserId().equals((String)event.getProperty(StoreListener.USERID_PROPERTY))){
			ignore = true;
		}
		return ignore;
	}
}