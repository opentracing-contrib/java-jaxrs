package io.opentracing.contrib.jaxrs2.itest.cxf;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerDefaultConfigurationTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class ApacheCXFServerDefaultConfigurationITest extends
    AbstractServerDefaultConfigurationTest {

  @Override
  protected void initServletContext(ServletContextHandler context) {
    ApacheCXFHelper.initServletContext(context);
  }
}
