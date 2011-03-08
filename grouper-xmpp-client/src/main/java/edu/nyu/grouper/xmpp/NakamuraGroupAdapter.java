package edu.nyu.grouper.xmpp;

import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject;
import edu.nyu.grouper.xmpp.exceptions.GroupModificationException;

public interface NakamuraGroupAdapter {
	
	public void createGroup(String groupId) throws GroupModificationException;
	
	public void deleteGroup(String groupId) throws GroupModificationException;
	
	public void addMembership(String groupId, GrouperClientXmppSubject subject) throws GroupModificationException;
	
	public void deleteMembership(String groupId, GrouperClientXmppSubject subject) throws GroupModificationException;

}
