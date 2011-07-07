package org.sakaiproject.nakamura.grouper.event;

import javax.jms.JMSException;

import org.apache.solr.client.solrj.SolrServerException;

public interface BatchOperationsManager {

	/**
	 * Cause all group and course information to be sent to Grouper
	 * @throws SolrServerException
	 * @throws JMSException
	 */
	public abstract void doGroups() throws SolrServerException, JMSException;

	/**
	 * Cause all contact information to be sent to Grouper
	 * @throws SolrServerException
	 * @throws JMSException
	 */
	public abstract void doContacts() throws SolrServerException, JMSException;
	
	/**
	 * Cause one group to be sync'd to Grouper.
	 * Useful for debugging/testing.
	 * @param groupId the id of the group to sync.
	 * @throws JMSException
	 */
	public abstract void doOneGroup(String groupId) throws JMSException;

}