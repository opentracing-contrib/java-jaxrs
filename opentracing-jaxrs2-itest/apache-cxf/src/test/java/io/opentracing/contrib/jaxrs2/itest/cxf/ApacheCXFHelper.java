package io.opentracing.contrib.jaxrs2.itest.cxf;

import io.opentracing.contrib.jaxrs2.itest.common.rest.InstrumentedRestApplication;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author Pavol Loffay
 */
public class ApacheCXFHelper {
  private ApacheCXFHelper() {}

  public static void initServletContext(ServletContextHandler context) {
    ServletHolder apacheCXFServlet = context.addServlet(
        org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet.class, "/*");
    apacheCXFServlet.setInitOrder(0);

    apacheCXFServlet.setInitParameter(
        "javax.ws.rs.Application", InstrumentedRestApplication.class.getCanonicalName());
  }
}
