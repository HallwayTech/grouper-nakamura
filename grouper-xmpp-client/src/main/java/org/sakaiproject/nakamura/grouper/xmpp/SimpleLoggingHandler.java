package org.sakaiproject.nakamura.grouper.xmpp;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject;

public class SimpleLoggingHandler implements GrouperClientXmppHandler {
	
	private static final Logger log = LoggerFactory.getLogger(SimpleLoggingHandler.class); 

	public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob,
			String groupName, String groupExtension,
			List<GrouperClientXmppSubject> newSubjectList,
			List<GrouperClientXmppSubject> previousSubjectList,
			GrouperClientXmppSubject changeSubject, String action) {
		
		if (log.isDebugEnabled()){
			log.debug("jobName = {}\n groupName = {}\n groupExtension = {}\n previousSubjectList = {}\n, newSubjectList = {}\n changeSubject = {}\n, action = {}",
					new Object[] { grouperClientXmppJob.getJobName(), 
									groupName, 
									groupExtension, 
									SimpleLoggingHandler.joinSubjectIds(previousSubjectList), 
									SimpleLoggingHandler.joinSubjectIds(newSubjectList),
									changeSubject.getSubjectId(), 
									action }
					); 
		}
	}

	public void handleAll(GrouperClientXmppJob grouperClientXmppJob,
			String groupName, String groupExtension,
			List<GrouperClientXmppSubject> newSubjectList) {
		
		if (log.isDebugEnabled()){
			log.debug("jobName = {}\n groupName = {}\n groupExtension = {}\n, newSubjectList = {}\n",
					new Object[] { grouperClientXmppJob.getJobName(), 
									groupName, 
									groupExtension, 
									SimpleLoggingHandler.joinSubjectIds(newSubjectList) }
					); 
		}
	}

	/**
	 * Flatten a List of {@link GrouperClientXmppSubject}s into a String
	 * @param s the Subjects 
	 * @return the list joined by commas
	 */
	public static String joinSubjectIds(List<GrouperClientXmppSubject> s) {
	    if (s.isEmpty()) return "";
	    Iterator<GrouperClientXmppSubject> iter = s.iterator();
	    StringBuffer buffer = new StringBuffer(iter.next().getSubjectId());
	    while (iter.hasNext()) buffer.append(", ").append(iter.next());
	    return buffer.toString();
	}

}
