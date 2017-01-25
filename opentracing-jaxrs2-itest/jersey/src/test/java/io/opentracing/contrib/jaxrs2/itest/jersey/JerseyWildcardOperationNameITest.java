package io.opentracing.contrib.jaxrs2.itest.jersey;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractWildcardOperationNameTest;
import io.opentracing.contrib.jaxrs2.itest.common.rest.InstrumentedRestApplication;

/**
 * @author Pavol Loffay
 */
public class JerseyWildcardOperationNameITest extends AbstractWildcardOperationNameTest {

    @Override
    public void initServletContext(ServletContextHandler context) {
        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(
                "javax.ws.rs.Application", InstrumentedRestApplication.class.getCanonicalName());
    }
}
