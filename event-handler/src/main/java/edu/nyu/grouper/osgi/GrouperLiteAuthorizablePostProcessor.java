package edu.nyu.grouper.osgi;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.ModificationType;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.user.LiteAuthorizablePostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupDeleteRequest;
import edu.nyu.grouper.api.GrouperIdHelper;
import edu.nyu.grouper.osgi.api.GrouperConfiguration;
import edu.nyu.grouper.util.GrouperHttpUtil;
import edu.nyu.grouper.util.GrouperJsonUtil;

@Component(immediate = true, metatype = true)
@Service
public class GrouperLiteAuthorizablePostProcessor implements
		LiteAuthorizablePostProcessor {

	private static final Logger log = LoggerFactory.getLogger(GrouperLiteAuthorizablePostProcessor.class);

	@Reference
	protected GrouperConfiguration grouperConfiguration;

	@Reference
	protected GrouperIdHelper groupIdHelper;

	public void process(SlingHttpServletRequest request,
			Authorizable authorizable, Session session, Modification change,
			Map<String, Object[]> parameters) throws Exception {

		// We only care about group deleted events.
		if (!authorizable.isGroup() || !change.getType().equals(ModificationType.DELETE)){
			return;
		}

		String fullGrouperName = (String)authorizable.getProperty("grouper:name");
		if (fullGrouperName == null){
			fullGrouperName = groupIdHelper.getGrouperName(authorizable.getId());
		}

		try {
			log.debug("Deleting Grouper Group = {} for sakai authorizableId = {}",
					fullGrouperName, authorizable.getId());

			HttpClient client = GrouperHttpUtil.getHttpClient(grouperConfiguration);
			// Create a POST
			String grouperWsRestUrl = grouperConfiguration.getRestWsGroupUrlString();
			PostMethod method = new PostMethod(grouperWsRestUrl);
			method.setRequestHeader("Connection", "close");

			// Fill out the group delete request beans
			WsRestGroupDeleteRequest groupDelete = new WsRestGroupDeleteRequest();
			groupDelete.setWsGroupLookups(new WsGroupLookup[]{ new WsGroupLookup(fullGrouperName, null) });

			// Encode the request and send it off
		    String requestDocument = GrouperJsonUtil.toJSONString(groupDelete);
				method.setRequestEntity(new StringRequestEntity(requestDocument, "text/x-json", "UTF-8"));

			client.executeMethod(method);
			log.debug("POST Method executed to {}.", grouperWsRestUrl);

			// Check the response
			Header successHeader = method.getResponseHeader("X-Grouper-success");
			String successString = successHeader == null ? null : successHeader.getValue();
			if (successString == null || successString.equals("")) {
				throw new Exception("Web service did not even respond!");
			}
			boolean success = "T".equals(successString);
			String resultCode = method.getResponseHeader("X-Grouper-resultCode").getValue();
			String responseString = IOUtils.toString(method.getResponseBodyAsStream());
			// JSONObject responseJSON = JSONObject.fromObject();

			// see if request worked or not
			if (!success) {
				throw new Exception("Bad response from web service: successString: " + successString
						+ ", resultCode: " + resultCode + ", " + responseString);
			}

			log.debug("Success! Delete Grouper Group = {} for sakai authorizableId = {}",
					fullGrouperName, authorizable.getId());
		}
		catch (StorageClientException sce) {
			log.error("Unable to fetch authorizable for " + authorizable.getId(), sce);
		}
		catch (AccessDeniedException ade) {
			log.error("Unable to fetch authorizable for " + authorizable.getId() + ". Access Denied.", ade);
		}
		catch (IOException ioe){
			log.error("IOException while communicating with grouper web services.", ioe);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}