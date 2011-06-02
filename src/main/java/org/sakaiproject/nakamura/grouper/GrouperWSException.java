package org.sakaiproject.nakamura.grouper;

import edu.internet2.middleware.grouperClient.ws.beans.WsResponseBean;

public class GrouperWSException extends GrouperException {

	private static final long serialVersionUID = 8716311980252611843L;

	public GrouperWSException(String message) {
		super(message);
	}

	public GrouperWSException(WsResponseBean response){
		super("Bad response from web service,"
				+ "responseCode: " + response.getResultMetadata().retrieveHttpStatusCode()
				+ ", resultCode: " + response.getResultMetadata().getResultCode()
				+ ", response: " + response.getResponseMetadata().getResultWarnings());
	}

}
