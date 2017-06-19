package io.opentracing.contrib.jaxrs2.itest.jersey;

import io.opentracing.contrib.jaxrs2.itest.common.rest.InstrumentedRestApplication;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author Pavol Loffay
 */
public class JerseyHelper {

    private JerseyHelper() {
    }

    public static void initServletContext(ServletContextHandler context) {
        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(
                "javax.ws.rs.Application", InstrumentedRestApplication.class.getCanonicalName());

    }

}
