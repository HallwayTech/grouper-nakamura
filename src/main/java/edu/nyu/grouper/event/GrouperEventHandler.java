package edu.nyu.grouper.event;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.sakaiproject.nakamura.api.lite.StoreListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nyu.grouper.GrouperException;
import edu.nyu.grouper.api.GrouperManager;
import edu.nyu.grouper.api.GrouperConfiguration;

@Service
@Component(immediate = true, metatype=true, enabled = false)
@Properties(value = { 
		@Property(name = EventConstants.EVENT_TOPIC, 
				value = {
				"org/sakaiproject/nakamura/lite/authorizables/ADDED",
				"org/sakaiproject/nakamura/lite/authorizables/UPDATED"
		})
})
public class GrouperEventHandler implements EventHandler {

	private Logger log = LoggerFactory.getLogger(GrouperEventHandler.class);

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
		
		String groupId = (String) event.getProperty("path");

		try {

			if ("org/sakaiproject/nakamura/lite/authorizables/ADDED".equals(event.getTopic())){

				// These events should be under org/sakaiproject/nakamura/lite/authorizables/UPDATED
				// http://jira.sakaiproject.org/browse/KERN-1795
				String membersAdded = (String)event.getProperty(GrouperEventUtils.MEMBERS_ADDED_PROP);
				if (membersAdded != null){
					grouperManager.addMemberships(groupId,
							Arrays.asList(StringUtils.split(membersAdded, ",")));
				} 

				String membersRemoved = (String)event.getProperty(GrouperEventUtils.MEMBERS_REMOVED_PROP);
				if (membersRemoved != null){
					grouperManager.removeMemberships(groupId,
							Arrays.asList(StringUtils.split(membersRemoved, ",")));
				}

				if (membersAdded == null && membersRemoved == null) {
					grouperManager.createGroup(groupId);
				}
			}

			if ("org/sakaiproject/nakamura/lite/authorizables/UPDATED".equals(event.getTopic())){
				grouperManager.updateGroup(groupId, event);
			}
		}
		catch (GrouperException e){
			log.error("An error occured while updating Grouper.", e);
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

		String type = (String)event.getProperty("type");
		if (type != null && !type.equals("group")){
			ignore = true;
		}

		return ignore;
	}
}