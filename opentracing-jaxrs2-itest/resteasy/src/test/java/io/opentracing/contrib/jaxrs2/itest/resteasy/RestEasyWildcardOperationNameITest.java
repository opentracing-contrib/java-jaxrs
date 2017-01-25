package io.opentracing.contrib.jaxrs2.itest.resteasy;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractWildcardOperationNameTest;
import io.opentracing.contrib.jaxrs2.itest.common.rest.InstrumentedRestApplication;

/**
 * @author Pavol Loffay
 */
public class RestEasyWildcardOperationNameITest extends AbstractWildcardOperationNameTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        ServletHolder jerseyServlet = context.addServlet(
                org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(
                "javax.ws.rs.Application", InstrumentedRestApplication.class.getCanonicalName());
    }
}
