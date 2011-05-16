package edu.nyu.grouper.event;

import java.util.Arrays;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.sakaiproject.nakamura.api.activemq.ConnectionFactoryService;
import org.sakaiproject.nakamura.api.lite.StoreListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nyu.grouper.api.GrouperManager;

@Component
public class GrouperJMSMessageConsumer {

	private static Logger log = LoggerFactory.getLogger(GrouperJMSMessageConsumer.class);

	private static String QUEUE_NAME = "org/sakaiproject/nakamura/grouper/sync";

	@Reference
	protected ConnectionFactoryService connFactoryService;

	@Reference
	protected GrouperManager grouperManager;
	
	private Connection connection;
	private Session session;
	private MessageConsumer consumer;

	@Activate
	public void activate(Map<?,?> props){
		try {
			connection = connFactoryService.getDefaultPooledConnectionFactory().createConnection();
			session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			Destination destination = session.createQueue(QUEUE_NAME);
			consumer = session.createConsumer(destination);
			consumer.setMessageListener(new GrouperMessageListener());
			connection.start();

		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}
	
	@Deactivate
	public void deactivate(){
		try {
			consumer.setMessageListener(null);
		}
		catch (JMSException jmse){
			log.error("Problem clearing the MessageListener.");
		}
		try {
			session.close();
		}
		catch (JMSException jmse){
			log.error("Problem closing JMS Session.");
		}
		finally {
			session = null;
		}
		
		try {
			connection.close();
		}
		catch (JMSException jmse){
			log.error("Problem closing JMS Connection.");
		}
		finally {
			connection = null;
		}
	}

	private class GrouperMessageListener implements MessageListener {

		public void onMessage(Message message){
			log.debug("Receiving a message on {} : {}", QUEUE_NAME, message);
			try {

				String groupId = (String) message.getStringProperty("path");
				String operation = "CREATE";

				if ("org/sakaiproject/nakamura/lite/authorizables/DELETED".equals(message.getStringProperty("event.topics"))){
					Map<String, Object> attributes = (Map<String,Object>)message.getObjectProperty(StoreListener.BEFORE_EVENT_PROPERTY);
					grouperManager.deleteGroup(groupId, attributes);
				}

				if ("org/sakaiproject/nakamura/lite/authorizables/ADDED".equals(message.getStringProperty("event.topics"))){

					// These events should be under org/sakaiproject/nakamura/lite/authorizables/UPDATED
					// http://jira.sakaiproject.org/browse/KERN-1795
					String membersAdded = (String)message.getStringProperty(GrouperEventUtils.MEMBERS_ADDED_PROP);
					if (membersAdded != null){
						// membership adds can be attached to the same event for the group add.
						grouperManager.createGroup(groupId);
						grouperManager.addMemberships(groupId,
								Arrays.asList(StringUtils.split(membersAdded, ",")));
						operation = "ADD_MEMBERS";
					}

					String membersRemoved = (String)message.getStringProperty(GrouperEventUtils.MEMBERS_REMOVED_PROP);
					if (membersRemoved != null){
						grouperManager.removeMemberships(groupId,
								Arrays.asList(StringUtils.split(membersRemoved, ",")));
						operation = "REMOVE_MEMBERS";
					}

					if (membersAdded == null && membersRemoved == null) {
						grouperManager.createGroup(groupId);
					}

					message.acknowledge();
					log.info("Successfully processed and acknowledged. {}, {}", operation, groupId);
				}
			}
			catch (JMSException jmse){
				log.error("JMSException while processing message.", jmse);
			}
			catch (Exception e){
				log.error("Exception while processing message.", e);
			}
		}
	}
}
