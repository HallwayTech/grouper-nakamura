package edu.nyu.grouper.event;

import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.sakaiproject.nakamura.api.activemq.ConnectionFactoryService;
import org.sakaiproject.nakamura.api.lite.StoreListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nyu.grouper.api.GrouperConfiguration;

@Service
@Component(immediate = true, metatype=true)
@Properties(value = { 
		@Property(name = EventConstants.EVENT_TOPIC, 
				value = {
				"org/sakaiproject/nakamura/lite/authorizables/ADDED",
				"org/sakaiproject/nakamura/lite/authorizables/UPDATED"
		})
})
/**
 * Capture Authorizable events and put them on a special Queue to be processed.
 */
public class GrouperJMSMessageProducer implements EventHandler {

	private static Logger log = LoggerFactory.getLogger(GrouperJMSMessageProducer.class);

	private static final String QUEUE_NAME = "org/sakaiproject/nakamura/grouper/sync";

	@Reference
	protected ConnectionFactoryService connFactoryService;

	@Reference
	protected GrouperConfiguration grouperConfiguration;

	/**
	 * @{inheritDoc}
	 * Respond to OSGi events by pushing them onto a JMS queue.
	 */
	@Override
	public void handleEvent(Event event) {
		if (ignoreEvent(event)){
			return;
		}

		try {
			Connection sender_connection = connFactoryService.getDefaultPooledConnectionFactory().createConnection();
			Session sender_session = sender_connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			Queue squeue = sender_session.createQueue(QUEUE_NAME);

			Message message = sender_session.createObjectMessage();
			copyEventToMessage(event, message);

			MessageProducer producer = sender_session.createProducer(squeue);
			sender_connection.start();
			producer.send(message);

			if (log.isDebugEnabled()){
				log.debug("Sent: " + message);
			}
			else if (log.isInfoEnabled()){
				log.info("Sent: {}, {}", event.getTopic(), (String)event.getProperty("path"));
			}

			sender_connection.close();

		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}

	/**
	 * @param event
	 * @return whether or not to ignore this event.
	 */
	private boolean ignoreEvent(Event event){
		boolean ignore = false;

		// Ignore events that were posted by the Grouper system to SakaiOAE. 
		// We don't want to wind up with a feedback loop between SakaiOAE and Grouper.
		String ignoreUser = grouperConfiguration.getIgnoredUserId();
		String eventCausedBy = (String)event.getProperty(StoreListener.USERID_PROPERTY); 
		if ( (ignoreUser != null && eventCausedBy != null) && 
			 (ignoreUser.equals(eventCausedBy))) {
				ignore = true;
		}

		// Ignore non-group events
		String type = (String)event.getProperty("type");
		if (type != null && !type.equals("group")){
			ignore = true;
		}

		// Ignore op=acl events
		String op = (String)event.getProperty("op");
		if (op != null && op.equals("acl")){
			ignore = true;
		}

		return ignore;
	}

	/**
	 * Stolen from org.sakaiproject.nakamura.events.OsgiJmsBridge
	 * @param event
	 * @param message
	 * @throws JMSException 
	 */
	public void copyEventToMessage(Event event, Message message) throws JMSException{
		for (String name : event.getPropertyNames()) {
			Object obj = event.getProperty(name);
			// "Only objectified primitive objects, String, Map and List types are
			// allowed" as stated by an exception when putting something into the
			// message that was not of one of these types.
			if (obj instanceof Byte || obj instanceof Boolean || obj instanceof Character
					|| obj instanceof Number || obj instanceof Map || obj instanceof String
					|| obj instanceof List) {
				message.setObjectProperty(name, obj);
			}
		}
	}
}
