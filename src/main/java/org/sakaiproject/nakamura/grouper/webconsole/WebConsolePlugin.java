package org.sakaiproject.nakamura.grouper.webconsole;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.osgi.framework.BundleContext;
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
	protected void renderContent(HttpServletRequest reques,
			HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().write("<label>Sync Groups and Contacts</label><button>Go</button>");
	}
}
