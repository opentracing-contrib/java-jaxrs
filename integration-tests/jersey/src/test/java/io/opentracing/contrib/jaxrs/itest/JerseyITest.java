package io.opentracing.contrib.jaxrs.itest;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.opentracing.contrib.jarxrs.itest.common.AbstractJettyTest;
import io.opentracing.contrib.jarxrs.itest.common.rest.InstrumentedRestApplication;

/**
 * @author Pavol Loffay
 */
public class JerseyITest extends AbstractJettyTest {

    @Override
    public void initServletContext(ServletContextHandler context) {
        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(
                "javax.ws.rs.Application", InstrumentedRestApplication.class.getCanonicalName());
    }
}
