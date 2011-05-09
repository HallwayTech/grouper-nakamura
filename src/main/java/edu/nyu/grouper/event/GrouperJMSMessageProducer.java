package edu.nyu.grouper.event;

import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.post.Modification;
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
import edu.nyu.grouper.api.GrouperManager;

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
				"org/sakaiproject/nakamura/lite/authorizables/UPDATED"
		})
})
public class GrouperJMSMessageProducer implements EventHandler, LiteAuthorizablePostProcessor {

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
			Connection senderConnection = connFactoryService.getDefaultPooledConnectionFactory().createConnection();
			Session senderSession = senderConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			Queue squeue = senderSession.createQueue(QUEUE_NAME);

			Message message = senderSession.createObjectMessage();
			copyEventToMessage(event, message);

			MessageProducer producer = senderSession.createProducer(squeue);
			senderConnection.start();
			producer.send(message);

			if (log.isDebugEnabled()){
				log.debug("Sent: " + message);
			}
			else if (log.isInfoEnabled()){
				log.info("Sent: {}, {}", event.getTopic(), (String)event.getProperty("path"));
			}

			senderConnection.close();

		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}
	
	/**
	 * Construct a delete message that has enough information to handle the delete in Grouper.
	 * This method comes from the {@link LiteAuthorizablePostProcessor} api. We use this instead 
	 * the OSGi EventAdmin interface because the {@link Authorizable} still exists at this point.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void process(SlingHttpServletRequest request,
			Authorizable authorizable,
			org.sakaiproject.nakamura.api.lite.Session session,
			Modification change, Map<String, Object[]> parameters)
			throws Exception {
		
		try {
			Connection senderConnection = connFactoryService.getDefaultPooledConnectionFactory().createConnection();
			Session senderSession = senderConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			Queue squeue = senderSession.createQueue(QUEUE_NAME);

			String topic = "org/sakaiproject/nakamura/lite/authorizables/DELETED";

			ObjectMessage msg = senderSession.createObjectMessage();
			msg.setStringProperty("event.topics", topic);
			msg.setStringProperty("path", authorizable.getId());
			msg.setStringProperty("type", "group");
			String grouperName = (String)authorizable.getProperty(GrouperManager.GROUPER_NAME_PROP);
			if (grouperName != null){
				msg.setStringProperty(GrouperManager.GROUPER_NAME_PROP, grouperName);
			}

			MessageProducer producer = senderSession.createProducer(squeue);
			senderConnection.start();
			producer.send(msg);

			if (log.isDebugEnabled()){
				log.debug("Sent: " + msg);
			}
			else if (log.isInfoEnabled()){
				log.info("Sent: {}, {}", topic, authorizable.getId());
			}
			senderConnection.close();
		}
		catch (JMSException e){
			log.error("An error occurred while sending a DELETED message.", e);
			throw e;
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
