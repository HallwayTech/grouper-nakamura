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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nyu.grouper.api.GrouperManager;

@Component(metatype=true)
public class GrouperJMSEventConsumer {

	private static Logger log = LoggerFactory.getLogger(GrouperJMSEventConsumer.class);

	private static String QUEUE_NAME = "org/sakaiproject/nakamura/grouper/sync";

	@Reference
	protected ConnectionFactoryService connFactoryService;

	@Reference
	protected GrouperManager grouperManager;
	
	private Connection connection;
	private Session session;

	@Activate
	public void activate(Map<?,?> props){
		try {
			connection = connFactoryService.getDefaultPooledConnectionFactory().createConnection();
			session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

			Destination destination = session.createQueue(QUEUE_NAME);
			MessageConsumer consumer = session.createConsumer(destination);

			consumer.setMessageListener(new GrouperMessageListener());
			connection.start();

		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}
	
	@Deactivate
	public void deactivate(){
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
			log.debug("Receiving a message on {}", QUEUE_NAME);

			try {
				String groupId = (String) message.getStringProperty("event.topics");

				if ("org/sakaiproject/nakamura/lite/authorizables/ADDED".equals(message.getStringProperty("osgi.topic"))){
					String membersAdded = (String)message.getStringProperty(GrouperEventUtils.MEMBERS_ADDED_PROP);
					String membersRemoved = (String)message.getStringProperty(GrouperEventUtils.MEMBERS_REMOVED_PROP);

					if (membersAdded != null){
						grouperManager.addMemberships(groupId, 
								Arrays.asList(StringUtils.split(membersAdded, ",")));
						message.acknowledge();
					} 
					else if (membersRemoved != null){
						grouperManager.removeMemberships(groupId, 
								Arrays.asList(StringUtils.split(membersRemoved, ",")));
						message.acknowledge();
					}
					else {
						grouperManager.createGroup(groupId);
						message.acknowledge();
					}
				}
			}
			catch (JMSException jmse){
				log.error("Exception while processing message.", jmse);
			}
		}
	}
}
