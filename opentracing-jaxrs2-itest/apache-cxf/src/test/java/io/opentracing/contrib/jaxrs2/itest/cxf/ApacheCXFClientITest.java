package io.opentracing.contrib.jaxrs2.itest.cxf;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractClientTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class ApacheCXFClientITest extends AbstractClientTest {

  @Override
  protected void initServletContext(ServletContextHandler context) {
    ApacheCXFHelper.initServletContext(context);
  }
}
