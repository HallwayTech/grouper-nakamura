package edu.nyu.grouper.event;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.sakaiproject.nakamura.api.activemq.ConnectionFactoryService;
import org.sakaiproject.nakamura.api.lite.StoreListener;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.user.LiteAuthorizablePostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nyu.grouper.api.GrouperConfiguration;

/**
 * Capture {@link Authorizable} events and put them on a special Queue to be processed.
 * 
 * When Groups are created or updated we are notified of an {@link Event} via the OSGi
 * {@link EventAdmin} service.
 * 
 * When Groups are deleted we are notified via the {@link LiteAuthorizablePostProcessor} service.
 * 
 * We then create a {@link Message} and place it on a {@link Queue}. The {@link GrouperJMSMessageConsumer}
 * will receive those messages and acknowledge them as they are successfully processed.
 */
@Service
@Component(immediate = true, metatype=true)
@Properties(value = { 
		@Property(name = EventConstants.EVENT_TOPIC, 
				value = {
				"org/sakaiproject/nakamura/lite/authorizables/ADDED",
				"org/sakaiproject/nakamura/lite/authorizables/UPDATED",
				"org/sakaiproject/nakamura/lite/authorizables/DELETE"
		})
})
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
		try {
			if (ignoreEvent(event) == false){
				sendMessage(event);
			}
		} 
		catch (JMSException e) {
			log.error("There was an error sending this event to the JMS queue", e);
		}
	}

	/**
	 * Convert an OSGi {@link Event} into a JMS {@link Message} and post it on a {@link Queue}.
	 * @param event the event we're sending
	 * @throws JMSException
	 */
	private void sendMessage(Event event) throws JMSException {
		Connection senderConnection = connFactoryService.getDefaultPooledConnectionFactory().createConnection();
		Session senderSession = senderConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		Queue squeue = senderSession.createQueue(QUEUE_NAME);
		MessageProducer producer = senderSession.createProducer(squeue);

		Message msg = senderSession.createObjectMessage();
		copyEventToMessage(event, msg);

		senderConnection.start();
		producer.send(msg);

		if (log.isDebugEnabled()){
			log.debug("Sent: " + msg);
		}
		else if (log.isInfoEnabled()){
			log.info("Sent: {}, {}", event.getTopic(), event.getProperty("path"));
		}

		senderConnection.close();
	}

	/**
	 * @param event
	 * @return whether or not to ignore this event.
	 */
	private boolean ignoreEvent(Event event){

		// Ignore events that were posted by the Grouper system to SakaiOAE. 
		// We don't want to wind up with a feedback loop between SakaiOAE and Grouper.
		String ignoreUser = grouperConfiguration.getIgnoredUserId();
		String eventCausedBy = (String)event.getProperty(StoreListener.USERID_PROPERTY); 
		if ( (ignoreUser != null && eventCausedBy != null) && 
			 (ignoreUser.equals(eventCausedBy))) {
				return true;
		}

		// Ignore non-group events
		String type = (String)event.getProperty("type");
		if (type != null && !type.equals("group")){
			return true;
		}

		// Ignore op=acl events
		String op = (String)event.getProperty("op");
		if (op != null && op.equals("acl")){
			return true;
		}

		for (String p: grouperConfiguration.getIgnoredGroups()){
			if (Pattern.matches(p, (String)event.getProperty(StoreListener.PATH_PROPERTY))){
				return true;
			}
		}

		return false;
	}

	/**
	 * Stolen from org.sakaiproject.nakamura.events.OsgiJmsBridge
	 * @param event
	 * @param message
	 * @throws JMSException 
	 */
	public static void copyEventToMessage(Event event, Message message) throws JMSException{
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
