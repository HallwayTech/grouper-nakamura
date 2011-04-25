package edu.nyu.grouper.osgi;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.ModificationType;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.user.LiteAuthorizablePostProcessor;
import org.sakaiproject.nakamura.user.lite.servlet.LiteAuthorizablePostProcessServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nyu.grouper.api.GrouperManager;

/**
 * We're using a {@link LiteAuthorizablePostProcessor} so we can access an object
 * that is about to be deleted. By the time we get an org/sakaiproject/nakamura/group/DELETED
 * event the group no longer exists.
 * 
 * NOTE: This currently doesn't work since the {@link LiteAuthorizablePostProcessServiceImpl}
 * doesn't have an active reference to a list of handlers.
 *
 */
@Component(immediate = true, metatype = true)
@Service
public class GrouperLiteAuthorizablePostProcessor implements
		LiteAuthorizablePostProcessor {

	private static final Logger log = LoggerFactory.getLogger(GrouperLiteAuthorizablePostProcessor.class);

	@Reference 
	protected GrouperManager grouperManager;

	public void process(SlingHttpServletRequest request,
			Authorizable authorizable, Session session, Modification change,
			Map<String, Object[]> parameters) throws Exception {

		if (!authorizable.isGroup() || !change.getType().equals(ModificationType.DELETE)){
			return; // We only care about group deleted events.
		}
		log.debug("Processing group : {}", authorizable.getId());
		grouperManager.deleteGroup(authorizable.getId());		
	}
}