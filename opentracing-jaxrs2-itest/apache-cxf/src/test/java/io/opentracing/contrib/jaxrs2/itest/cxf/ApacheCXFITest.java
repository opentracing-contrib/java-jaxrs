package io.opentracing.contrib.jaxrs2.itest.cxf;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractBasicTest;
import io.opentracing.contrib.jaxrs2.itest.common.rest.InstrumentedRestApplication;

/**
 * @author Pavol Loffay
 */
public class ApacheCXFITest extends AbstractBasicTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        ServletHolder apacheCXFServlet = context.addServlet(
                org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet.class, "/*");
        apacheCXFServlet.setInitOrder(0);

        apacheCXFServlet.setInitParameter(
                "javax.ws.rs.Application", InstrumentedRestApplication.class.getCanonicalName());
    }
}
