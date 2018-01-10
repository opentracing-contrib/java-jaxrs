package io.opentracing.contrib.jaxrs2.itest.jersey;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractClassOperationNameTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class JerseyClassOperationNameITest extends AbstractClassOperationNameTest {

  @Override
  public void initServletContext(ServletContextHandler context) {
    JerseyHelper.initServletContext(context);
  }
}
