/*
 * @author mchyzer
 * $Id: PrintTest.java,v 1.1 2009-06-10 05:31:35 mchyzer Exp $
 */
package edu.nyu.grouper.esb;

import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.nyu.grouper.xmpp.HttpNakamuraGroupAdapter;
import edu.nyu.grouper.xmpp.api.NakamuraGroupAdapter;

/**
 * Process changelog entries and update group information in sakai3-nakamura 
 */
public class NakamuraEsbConsumer extends ChangeLogConsumerBase {

	private static Log log = GrouperUtil.getLog(ChangeLogConsumerBase.class);
	
	private HttpNakamuraGroupAdapter nakamuraGroupAdapter;
	
	public NakamuraEsbConsumer(){
		super();
		nakamuraGroupAdapter = new HttpNakamuraGroupAdapter();
		nakamuraGroupAdapter.setUrl(GrouperLoaderConfig.getPropertyString("nakamura.url", true));
		nakamuraGroupAdapter.setUsername(GrouperLoaderConfig.getPropertyString("nakamura.username", true));
		nakamuraGroupAdapter.setPassword(GrouperLoaderConfig.getPropertyString("nakamura.password", true));
	}

	/**
	 * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase#processChangeLogEntries(List, ChangeLogProcessorMetadata)
	 */
	@Override
	public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
			ChangeLogProcessorMetadata changeLogProcessorMetadata) {

		long currentId = -1;

		// try catch so we can track that we made some progress
		try {
			for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
				currentId = changeLogEntry.getSequenceNumber();

				if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD)) {
					String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.id);
					String groupExtension = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name);
					log.debug("Group add, name: " + groupExtension);
					getNakamuraGroupAdapter().createGroup(groupId, groupExtension);
				}

				if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)) {
					String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.id);
					String groupExtension = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name);
					log.debug("Group delete, name: " + groupExtension );
					getNakamuraGroupAdapter().deleteGroup(groupId, groupExtension);
				}

				if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_UPDATE)) {
					String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.id);
					String propertyName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged);
					String oldValue = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue);
					String newValue = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyNewValue);
					
					// TODO implement updateProperty
					// nakamuraGroupAdapter.updateProperty(groupId, propertyName, oldValue, newValue);
					log.debug("Group update, name: "  + groupId + ", property: " + propertyName
							+ ", from: '" + oldValue + "', to: '" + newValue + "'");
				}

				if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
					String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId);
					String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
					String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
					log.debug("Membership add, name: " + groupName + " subjectId: " + subjectId);
					getNakamuraGroupAdapter().addMembership(groupId, groupName, subjectId);
				}

				if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
					String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId);
					String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
					String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
					log.debug("Membership delete, name: " + groupName + " subjectId: " + subjectId);
					getNakamuraGroupAdapter().deleteMembership(groupId, groupName, subjectId);
				}
				// we successfully processed this record
			}
		}
		catch (Exception e) {
			changeLogProcessorMetadata.registerProblem(e, "Error processing record", currentId);
			//we made it to this -1
			return currentId - 1;
		}
		if (currentId == -1) {
			throw new RuntimeException("Couldn't process any records");
		}
		return currentId;
	}

	public HttpNakamuraGroupAdapter getNakamuraGroupAdapter() {
		return nakamuraGroupAdapter;
	}

	public void setNakamuraGroupAdapter(HttpNakamuraGroupAdapter nakamuraGroupAdapter) {
		this.nakamuraGroupAdapter = nakamuraGroupAdapter;
	}
}