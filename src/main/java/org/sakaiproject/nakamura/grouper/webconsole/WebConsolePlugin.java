package org.sakaiproject.nakamura.grouper.webconsole;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jms.JMSException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.apache.solr.client.solrj.SolrServerException;
import org.osgi.framework.BundleContext;
import org.sakaiproject.nakamura.grouper.event.BatchOperationsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service(value=javax.servlet.Servlet.class)
@Component
public class WebConsolePlugin extends AbstractWebConsolePlugin {

	private static final long serialVersionUID = -810270792160656239L;

	private static Logger log = LoggerFactory.getLogger(WebConsolePlugin.class);

	public static final String LABEL = "grouper";
	@Property(value=LABEL)
	private static final String PROP_LABEL = "felix.webconsole.label";

	public static final String TITLE = "Grouper Sync";
	@Property(value=TITLE)
	private static final String PROP_TITLE = "felix.webconsole.title";

	@Reference
	Repository jcrRepository;

	@Reference
	BatchOperationsManager batchManager;
	
	@Activate
	public void activate(BundleContext bundleContext){
		log.error("activated");
		super.activate(bundleContext);
	}

	@Override
	public String getTitle() {
		// TODO internationalize
		return TITLE;
	}
	
	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	protected void renderContent(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Session jcrSession = null;
		try {
			if (request.getParameter("doGroups") != null){
				batchManager.doGroups();
			}
			if (request.getParameter("doContacts") != null){
				batchManager.doContacts();
			}
			if (request.getParameter("groupId") != null){
				batchManager.doOneGroup(request.getParameter("groupId"));
			}

			jcrSession = jcrRepository.login();
			Node node = jcrSession.getNode("/var/grouper/webconsole/plugin.html");
			InputStream content = node.getNode("jcr:content").getProperty("jcr:data").getBinary().getStream();
			IOUtils.copy(content, response.getWriter());
			jcrSession.logout();

		} catch (LoginException e) {
			log.error("Error logging in.", e);
		} catch (NoSuchWorkspaceException e) {
			log.error("No such workspace.", e);
		} catch (RepositoryException e) {
			log.error("Repository exception.", e);
		} catch (SolrServerException e) {
			log.error("Solr search exception.", e);
		} catch (JMSException e) {
			log.error("JMS messaging exception.", e);
		}
		finally {
			if (jcrSession != null){
				jcrSession = null;
			}
		}
	}
}
